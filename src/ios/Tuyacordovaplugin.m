#import "Tuyacordovaplugin.h"
#import "AppDelegate+Tuyacordovaplugin.h"
#import "CameraViewController.h"

@interface Tuyacordovaplugin () <TuyaSmartActivatorDelegate, TuyaSmartDeviceDelegate>
@property(strong, nonatomic) TuyaSmartHomeManager *homeManager;
@property (strong, nonatomic) TuyaSmartDevice *tyDevice;
//@property (nonatomic, strong) UIViewController *viewController;
@end

@implementation Tuyacordovaplugin

static Tuyacordovaplugin* tuyacordovaplugin;

@synthesize bgNotificationData;

+ (Tuyacordovaplugin*) tuyacordovaplugin {
    return tuyacordovaplugin;
}

- (void) pluginInitialize {
    @try {
        tuyacordovaplugin = self;
        NSString *appKey = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleTuyaAppKey"];
        NSString *appSecret = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleTuyaAppSecret"];
        [[TuyaSmartSDK sharedInstance] startWithAppKey:appKey secretKey:appSecret];
    } 
    @catch (NSException* e) {
        NSLog(@"Tuya init err", e);
    }
}

- (void) user_loginOrRegitserWithUID: (CDVInvokedUrlCommand *) command {

    NSString *countryCode = (NSString *)[command argumentAtIndex:0];
     NSString *uid = (NSString *)[command argumentAtIndex:1];
      NSString *pass = (NSString *)[command argumentAtIndex:2];

    [[TuyaSmartUser sharedInstance] loginOrRegisterWithCountryCode:countryCode uid:uid password:pass createHome:YES success:^(id result) {
        
        
        [self.homeManager getHomeListWithSuccess:^(NSArray<TuyaSmartHomeModel *> *homes) {
            if (homes && homes[0] && homes[0].homeId) {
            NSDictionary *resultDict = @{
                @"homeId": [NSString stringWithFormat:@"%lld", homes[0].homeId]
            };
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            } else {
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Internal Error TNH001"];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
            } failure:^(NSError *errorMsg) {
                NSLog(@"loginOrRegisterWithCountryCode failure: %@", errorMsg);
                NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    } failure:^(NSError *errorMsg) {
        NSLog(@"loginOrRegisterWithCountryCode failure: %@", errorMsg);
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(void) user_isLoggedIn: (CDVInvokedUrlCommand *) command {
    Boolean isLoggedIn = [TuyaSmartUser sharedInstance].isLogin;
    NSDictionary *resultDict = @{
        @"status": isLoggedIn ? @YES: @NO
    };
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) user_logout: (CDVInvokedUrlCommand *) command {
    [[TuyaSmartUser sharedInstance] loginOut:^{
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } failure:^(NSError *errorMsg) {
        NSLog(@"logout failure: %@", errorMsg);
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}


- (void) network_smartCameraConfiguration: (CDVInvokedUrlCommand *) command {
    NSString *ssid = (NSString *)[command argumentAtIndex:0];
    NSString *pass = (NSString *)[command argumentAtIndex:1];
    NSString *homeIdString = (NSString *)[command argumentAtIndex:2];
    long long homeId = [homeIdString longLongValue];
    
    self.latestCallbackIdForConfiguration = command.callbackId;

    [[TuyaSmartActivator sharedInstance] getTokenWithHomeId:homeId success:^(NSString *result) {
        if (result && result.length > 0){
            [TuyaSmartActivator sharedInstance].delegate = self;
            NSDictionary *dictionary = @{
                    @"s": ssid,
                    @"p": pass,
                    @"t": result
            };
            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:0 error:nil];
            NSString *wifiJsonStr = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

            NSDictionary *resultDict = @{
                @"status": @"qr",
                @"qrCode": wifiJsonStr
            };
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
            [pluginResult setKeepCallbackAsBool:true];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [[TuyaSmartActivator sharedInstance] startConfigWiFi:TYActivatorModeQRCode ssid:ssid password:pass token:result timeout:100];
            });
        } else {
            [TuyaSmartActivator sharedInstance].delegate = nil;
            self.latestCallbackIdForConfiguration = nil;
            NSDictionary* resultDict = @{
                @"message": @"Cannot generate QR Code, try again",
                @"code": @"KTCUST001"
            };
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    } failure:^(NSError *errorMsg) {
        [TuyaSmartActivator sharedInstance].delegate = nil;
        self.latestCallbackIdForConfiguration = nil;
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) network_stopCameraConfiguration: (CDVInvokedUrlCommand *) command {
    [TuyaSmartActivator sharedInstance].delegate = nil;
    [[TuyaSmartActivator sharedInstance] stopConfigWiFi];
}

- (void) home_listHomes: (CDVInvokedUrlCommand *) command {
    [self.homeManager getHomeListWithSuccess:^(NSArray<TuyaSmartHomeModel *> *homes) {
        if (!homes) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:[[NSMutableArray alloc] init]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:homes];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    } failure:^(NSError *errorMsg) {
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) home_listDevices: (CDVInvokedUrlCommand *) command {

    NSString *homeIdString = (NSString *)[command argumentAtIndex:0];
    long long homeId = [homeIdString longLongValue];


    TuyaSmartHome *home = [TuyaSmartHome homeWithHomeId:homeId];
    home.delegate = self;
    [home getHomeDetailWithSuccess:^(TuyaSmartHomeModel *homeModel) {
        NSArray<TuyaSmartDeviceModel*> *devices = [home.deviceList mutableCopy];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

        } failure:^(NSError *errorMsg) {
            NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
}



- (void) ipc_startCameraLivePlay: (CDVInvokedUrlCommand *) command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];
    NSString *bgColor = (NSString *)[command argumentAtIndex:1];
    NSString *primaryColor = (NSString *)[command argumentAtIndex:2];
    NSString *itemBgColor = (NSString *)[command argumentAtIndex:3];
    NSString *textColor1 = (NSString *)[command argumentAtIndex:4];
    NSString *textColor2 = (NSString *)[command argumentAtIndex:5];
    NSDictionary *params = @{
            @"devId": devId,
            @"bgColor": bgColor,
            @"primaryColor": primaryColor,
            @"itemBgColor": itemBgColor,
            @"textColor1": textColor1,
            @"textColor2": textColor2
    };
    CameraViewController *vc = [[CameraViewController alloc] initWithDeviceId:params];
    //  [self.navigationController pushViewController:vc animated:YES];


//    [self.viewController addChildViewController:vc];
//    [self.webView.superview insertSubview:vc.view aboveSubview:self.webView];
    
    // [self.viewController presentModalViewController:vc  animated:YES];
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:vc];
    [self.viewController presentViewController:navigationController animated:YES completion:nil];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void) device_data: (CDVInvokedUrlCommand *)command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];
    TuyaSmartDevice *device = [TuyaSmartDevice deviceWithDeviceId:devId];
    NSDictionary *dict = @{
        @"devId": device.deviceModel.devId,
        @"ip": device.deviceModel.ip,
        @"timezoneId": device.deviceModel.timezoneId,
        @"dps": device.deviceModel.dps
    };
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict
                                                  options:(NSJSONWritingOptions) 0
                                                    error:&error];

    if (! jsonData) {
        NSDictionary *err = @{
            @"code": @"KTCUST002",
            @"message": @"Unable to get get device details, try again"
        };
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:err];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    

}

- (void) setDPs: (CDVInvokedUrlCommand *)command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];
    NSString *dps = (NSString *)[command argumentAtIndex:1];
    
    NSData *data = [dps dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];

    [self initTyDeviceWithDevId:devId];
    
    TuyaSmartSchemaModel *schema = self.tyDevice.deviceModel.schemaArray[2];
    
    [self.tyDevice publishDps: json success:^(){
        self.latestCallbackIdForDpsUpdate = command.callbackId;
    } failure:^(NSError *errorMsg) {
        [self destroyTyDevice];
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
    //TODO make this whole function work
}

- (void) renameDevice: (CDVInvokedUrlCommand *)command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];
    NSString *deviceName = (NSString *)[command argumentAtIndex:1];

    [self initTyDeviceWithDevId:devId];
    
    [self.tyDevice updateName:deviceName success:^{
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:(NSString *)deviceName];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } failure:^(NSError *errorMsg) {
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) removeDevice: (CDVInvokedUrlCommand *)command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];
    NSString *deviceName = (NSString *)[command argumentAtIndex:1];

    TuyaSmartDevice *smartDevice = [TuyaSmartDevice deviceWithDeviceId:devId];
    [smartDevice remove:^{
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:(NSString *)deviceName];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } failure:^(NSError *errorMsg) {
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) signalStrength: (CDVInvokedUrlCommand *)command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];

    [self initTyDeviceWithDevId:devId];
    
    [self.tyDevice getWifiSignalStrengthWithSuccess:^(){
        self.latestCallbackIdForSignalStrength = command.callbackId;
    } failure:^(NSError *errorMsg) {
        [self destroyTyDevice];
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) push_getBgNotificationData: (CDVInvokedUrlCommand *)command {
    if (self.bgNotificationData != nil) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:self.bgNotificationData];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
//        self.bgNotificationData = nil;
    } else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:nil];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) push_getForegroundNotificationData: (CDVInvokedUrlCommand *)command {
    self.latestCallbackIdForForegroundNotification = command.callbackId;
    if (self.bgNotificationData != nil) {
        [self sendForegroundNotification:self.bgNotificationData];
        return;
    }
    else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [pluginResult setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}



- (TuyaSmartHomeManager *)homeManager {
    if (!_homeManager) {
        _homeManager = [[TuyaSmartHomeManager alloc] init];
    }
    return _homeManager;
}

- (void) initTyDeviceWithDevId: devId {
    self.tyDevice = [TuyaSmartDevice deviceWithDeviceId:devId];
    self.tyDevice.delegate = self;
}

- (void) destroyTyDevice {
    self.tyDevice = nil;
}

- (void) saveBgNotificationData: (NSDictionary *)userData {
    if (self.latestCallbackIdForForegroundNotification && self.latestCallbackIdForForegroundNotification.length > 0) {
        [self sendForegroundNotification:userData];
    } else {
        self.bgNotificationData = userData;
    }
}

- (void) sendForegroundNotification: (NSDictionary *)userData {
    if (self.latestCallbackIdForForegroundNotification && self.latestCallbackIdForForegroundNotification.length > 0) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:userData];
        [pluginResult setKeepCallbackAsBool:true];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.latestCallbackIdForForegroundNotification];
    }
}

//helpers
+ (NSDictionary *)makeError:(NSError *)errorMessage {
    NSString *message = errorMessage.userInfo[@"NSLocalizedDescription"] ? errorMessage.userInfo[@"NSLocalizedDescription"] : @"Unknown Error Occurred";
    NSInteger code = [errorMessage code];
    NSDictionary *resultDict = @{
        @"message": message,
        @"code": [NSString stringWithFormat: @"%ld", (long)code]
    };
  return resultDict;
}

#pragma mark - TuyaSmartActivatorDelegate

- (void)activator:(TuyaSmartActivator *)activator didReceiveDevice:(TuyaSmartDeviceModel *)deviceModel error:(NSError *)error {
    if (self.latestCallbackIdForConfiguration && self.latestCallbackIdForConfiguration.length > 0) {
        if (deviceModel && error == nil) {
            NSDictionary *resultDict = @{
                @"status": @"success",
                @"deviceId": deviceModel.devId,
                @"mac": deviceModel.uuid,
                @"deviceName": deviceModel.name
            };
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.latestCallbackIdForConfiguration];
        }
        if (error) {
            NSDictionary *resultDict = [Tuyacordovaplugin makeError:error];
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.latestCallbackIdForConfiguration];
        }
        [TuyaSmartActivator sharedInstance].delegate = nil;
        self.latestCallbackIdForConfiguration = nil;
    }
}


#pragma mark - TuyaSmartDeviceDelegate

-(void)device:(TuyaSmartDevice *)device dpsUpdate:(NSDictionary *)dps {
    
    if (self.latestCallbackIdForDpsUpdate && self.latestCallbackIdForDpsUpdate.length > 0) {
        NSDictionary *resultDict = @{
            @"status": @"success",
            @"dps": device.deviceModel.dps,
        };
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.latestCallbackIdForDpsUpdate];
    }
    [self destroyTyDevice];
    self.latestCallbackIdForDpsUpdate = nil;
}

- (void)device:(TuyaSmartDevice *)device signal:(NSString *)signal {
    if (self.latestCallbackIdForSignalStrength && self.latestCallbackIdForSignalStrength.length > 0) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:signal];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.latestCallbackIdForSignalStrength];
    }
    [self destroyTyDevice];
    self.latestCallbackIdForSignalStrength = nil;
}


@end

