//
//  CameraViewController.h
//  TuyaSmartIPCDemo
//
//  Copyright (c) 2014-2021 Tuya Inc. (https://developer.tuya.com/)

#import <UIKit/UIKit.h>
#import "CameraViewConstants.h"

@interface CameraViewController : UIViewController

- (instancetype)initWithDeviceId:(NSDictionary *)params;
-  (UIColor *) stringToColor:(NSString *) color;

@end
