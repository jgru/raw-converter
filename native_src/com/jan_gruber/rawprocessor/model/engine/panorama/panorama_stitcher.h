/* $Author: jgruber $ */

/** \file
 * The PanoramaStitcher class houses the functionality to stitch several partial images
 * together to one mosaic composite. It controls the whole process of image registration,
 * image matching and the actual stitching by applying a simple form of minimum boundary cut.
 */

#ifndef PanoramaStitcher_h
#define PanoramaStitcher_h

#include "raw_image.h"
#include "feature_detector.h"
#include <vector>

class PanoramaStitcher {
public:
	/**
	 * This struct houses the parameters, which define, how the images
	 * willb e registered and merged.
	 */
	struct StitchingParams {
		FeatureDetector::FeatureDetectionParams feature_params;
		//specifies the size of feathering on the x-axis, which is
		//performed on the minimum error boundary
		int feather_size;
		//specifies, which algorithm to use for the seamline computation
		int seamline_code;
		//specifies, whether the seamline should be visualized or not
		bool is_visualize_seamline;
	};

	struct Correspondance {
		FeatureDetector::FeatureDescriptor f1;
		FeatureDetector::FeatureDescriptor f2;
		//mean square error specifies the difference between
		//the two features
		int mse;
	};

	PanoramaStitcher(StitchingParams params);
	PanoramaStitcher();
	~PanoramaStitcher();
	PanoramaStitcher(const PanoramaStitcher& that);
	PanoramaStitcher& operator=(const PanoramaStitcher& that);

	/**
	 Starts the process of stitching several partial images.

	 @param images A vector, which contains all partial images to stitch
	 @param params parameters for image registration and matching
	 @return all correspondances
	 */
	void Stitch(std::vector<RawImage*> images);

	RawImage* get_output();

private:
	//specifies the behaviour of the PanoramaStitcher
	StitchingParams params_;
	//the associated FeatureDetector to perform the image registration
	FeatureDetector* detector_;
	//the resulting mosaic/panorama
	RawImage* out_image_;

	/**
	 * Creates and initializes the out_image_ with the data from the first partial image
	 *
	 * @param base_img the first partial image
	 *
	 */
	void InitOutput(RawImage* base_img);

	/**
	 Returns the pairs of corresponding features in to partial images.

	 @param m_features the FeatureDescriptors of the interest points detected in the first image
	 @param o_features the FeatureDescriptors of the interest points detected in the second image

	 @return all correspondances
	 */
	std::vector<Correspondance> matchFeatures(
			std::vector<FeatureDetector::FeatureDescriptor> m_features,
			std::vector<FeatureDetector::FeatureDescriptor> o_features);

	/**
	 Returns the mean squared error

	 @param f1 one FeatureDescriptor of one interest point of the first image
	 @param f2 one FeaturDescriptor of one interest point of the second image

	 @return the mean squared error
	 */
	int CalcMSE(FeatureDetector::FeatureDescriptor f1,
			FeatureDetector::FeatureDescriptor f2);

	/**
	 * Calculates the homography based on the given correspondances
	 *
	 * @param translation a pointer to an array, which will be filled by the calculated translation
	 * of the second image dependent on the first one.
	 *
	 * @param the correspondances of the the two partial images
	 *
	 */
	void CalcHomography(int* translation, std::vector<Correspondance> corrs);

	/**
	 * Creates a composite of the out_image_ and the img_to_add,
	 * then it stores the result in the out_image_ member variable
	 * In the course of stitching these images together a simple form of minimum error boundary
	 * cut or an error avoiding diagonal cut is computet.
	 * This seamline is feathered by the specified size.
	 *
	 * @param base_img the base image
	 * @param img_to_add the image to add
	 * @param trans the homography of the img_to_add
	 * @param feather_size_x the size of the feathering in x-direction
	 *
	 */
	void CreateRawComposite(RawImage* img_to_add, int* trans);

	std::vector<uint16_t> ComputeMinErrorBoundaryCut(RawImage* img_to_add,
			int out_width, int out_height, int diff_x, int diff_y,
			bool is_swapped);
	std::vector<uint16_t> ComputeSimpleBoundaryCut(RawImage* img_to_add,
			int out_width, int out_height, int diff_x, int diff_y,
			bool is_swapped);

};
#endif
