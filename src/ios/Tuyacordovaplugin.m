#import "Tuyacordovaplugin.h"

@interface Tuyacordovaplugin ()
@property(strong, nonatomic) TuyaSmartHomeManager *homeManager;
@end

@implementation Tuyacordovaplugin

- (void) pluginInitialize {
    NSString *appKey = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleTuyaAppKey"];
    NSString *appSecret = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleTuyaAppSecret"];
    [[TuyaSmartSDK sharedInstance] startWithAppKey:appkey secretKey:appSecret];
}

- (void) user_loginOrRegitserWithUID: (CDVInvokedUrlCommand *) command {

	NSString *countryCode = (NSString *)[command argumentAtIndex:0];
 	NSString *uid = (NSString *)[command argumentAtIndex:1];
  	NSString *pass = (NSString *)[command argumentAtIndex:2];

    [[TuyaSmartUser sharedInstance] loginOrRegisterWithCountryCode:countryCode uid:uid password:pass createHome:YES success:^(id result) {
		NSDictionary *resultDict = [NSDictionary dictionaryWithObjectsAndKeys:
        [NSString numberWithLongLong:result], @"homeId",
      	nil];
		CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	} failure:^(NSError *errorMsg) {
        NSLog(@"loginOrRegisterWithCountryCode failure: %@", errorMsg);
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	}];
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

    NSArray<TuyaSmartDeviceModel*> *devices = [[TuyaSmartHome homeWithHomeId:homeId].deviceList mutableCopy];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:devices];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    } failure:^(NSError *errorMsg) {
        NSDictionary *resultDict = [Tuyacordovaplugin makeError:errorMsg];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void) ipc_startCameraLivePlay: (CDVInvokedUrlCommand *) command {
    NSString *devId = (NSString *)[command argumentAtIndex:0];
    
}


- (TuyaSmartHomeManager *)homeManager {
    if (!_homeManager) {
        _homeManager = [[TuyaSmartHomeManager alloc] init];
    }
    return _homeManager;
}


//helpers
+ (NSDictionary *)makeError:(NSString *)errorMessage {
  NSDictionary *resultDict = [NSDictionary dictionaryWithObjectsAndKeys:
    errorMessage, @"message",
  nil];
  return resultDict;
}

@end