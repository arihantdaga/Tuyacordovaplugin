//
//  CameraPTZControlView.h
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface CameraPTZControlView : UIView
@property (nonatomic, copy) NSString *deviceId;
@property (nonatomic, weak) UIViewController *fatherVc;

- (void) mountUI;
@end

NS_ASSUME_NONNULL_END
