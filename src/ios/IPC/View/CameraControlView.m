//
//  CameraControlView.m
//  TuyaSmartIPCDemo
//
//  Copyright (c) 2014-2021 Tuya Inc. (https://developer.tuya.com/)

#import "CameraControlView.h"
#import "CameraControlButton.h"
#import "CameraViewController.h"

@interface CameraControlView ()

@end

@implementation CameraControlView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {

    }
    return self;
}

- (void)drawRect:(CGRect)rect {
    CGFloat width = self.frame.size.width;
    CGFloat height = self.frame.size.height;
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    CGContextMoveToPoint(ctx, 0, 0);
    CGContextAddLineToPoint(ctx, width, 0);
    
    CGContextMoveToPoint(ctx, width / 3, 0);
    CGContextAddLineToPoint(ctx, width / 3, height);
    CGContextMoveToPoint(ctx, width / 3 * 2, 0);
    CGContextAddLineToPoint(ctx, width / 3 * 2, height);
    
    CGContextMoveToPoint(ctx, 0, height);
    CGContextAddLineToPoint(ctx, width, height);

}

- (void)setSourceData:(NSArray *)sourceData {
    [self removeAllSubviews];
    CGFloat buttonWith = self.frame.size.width / 3;
    CGFloat buttonHeight = self.frame.size.height /2;
    [sourceData enumerateObjectsUsingBlock:^(NSDictionary *obj, NSUInteger idx, BOOL *stop) {
        NSString *imageName = [obj objectForKey:@"image"];
        NSString *title = [obj objectForKey:@"title"];
        NSString *identifier = [obj objectForKey:@"identifier"];
        CameraControlButton *controlButton = [CameraControlButton new];
        controlButton.imageView.image = [[UIImage imageNamed:imageName] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate];
        controlButton.titleLabel.text = title;
        controlButton.identifier = identifier;
        controlButton.themeParams =  [self themeParams];
        [controlButton addTarget:self action:@selector(controlAction:)];
        controlButton.frame = CGRectMake(((idx) % 3) * (buttonWith + 0.5), (idx) / 3 * (buttonHeight + 0.5), buttonWith, buttonHeight);
        [self addSubview:controlButton];
    }];
}

- (void)enableControl:(NSString *)identifier {
    [self.subviews enumerateObjectsUsingBlock:^(CameraControlButton *obj, NSUInteger idx, BOOL *stop) {
        if ([obj.identifier isEqualToString:identifier]) {
            obj.disabled = NO;
            *stop = YES;
        }
    }];
}

- (void)disableControl:(NSString *)identifier {
    [self.subviews enumerateObjectsUsingBlock:^(CameraControlButton *obj, NSUInteger idx, BOOL *stop) {
        if ([obj.identifier isEqualToString:identifier]) {
            obj.disabled = YES;
            *stop = YES;
        }
    }];
}

- (void)selectedControl:(NSString *)identifier {
    [self.subviews enumerateObjectsUsingBlock:^(CameraControlButton *obj, NSUInteger idx, BOOL *stop) {
        if ([obj.identifier isEqualToString:identifier]) {
            obj.highLighted = YES;
            *stop = YES;
        }
    }];
}

- (void)deselectedControl:(NSString *)identifier {
    [self.subviews enumerateObjectsUsingBlock:^(CameraControlButton *obj, NSUInteger idx, BOOL *stop) {
        if ([obj.identifier isEqualToString:identifier]) {
            obj.highLighted = NO;
            *stop = YES;
        }
    }];
}

- (void)enableAllControl {
    [self.subviews enumerateObjectsUsingBlock:^(CameraControlButton *obj, NSUInteger idx, BOOL *stop) {
        obj.disabled = NO;
    }];
}

- (void)disableAllControl {
    [self.subviews enumerateObjectsUsingBlock:^(CameraControlButton *obj, NSUInteger idx, BOOL *stop) {
        obj.disabled = YES;
    }];
}

- (void)controlAction:(UITapGestureRecognizer *)recognizer {
    CameraControlButton *controlButton = (CameraControlButton *)recognizer.view;
    if (!controlButton.disabled) {
        [self.delegate controlView:self didSelectedControl:controlButton.identifier];
    }
}

- (void)removeAllSubviews {
    [self.subviews enumerateObjectsUsingBlock:^(__kindof UIView * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj removeFromSuperview];
    }];
}

@end
