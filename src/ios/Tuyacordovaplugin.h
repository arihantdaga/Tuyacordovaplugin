#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>
#import <TuyaSmartActivatorKit/TuyaSmartActivatorKit.h>

@interface Tuyacordovaplugin: CDVPlugin

- (void) user_loginOrRegitserWithUID: (CDVInvokedUrlCommand *)command;
- (void) user_isLoggedIn: (CDVInvokedUrlCommand *)command;

- (void) home_listHomes: (CDVInvokedUrlCommand *)command;
- (void) home_listDevices: (CDVInvokedUrlCommand *)command;
// TODO
// - (void) home_initNotifications: (CDVInvokedUrlCommand *)command;

- (void) ipc_startCameraLivePlay: (CDVInvokedUrlCommand *)command;

- (void) network_smartCameraConfiguration: (CDVInvokedUrlCommand *)command;
- (void) network_stopCameraConfiguration: (CDVInvokedUrlCommand *)command;

- (void) device_data: (CDVInvokedUrlCommand *)command;
- (void) setDPs: (CDVInvokedUrlCommand *)command;
- (void) renameDevice: (CDVInvokedUrlCommand *)command;
- (void) removeDevice: (CDVInvokedUrlCommand *)command;
- (void) signalStrength: (CDVInvokedUrlCommand *)command;

- (void) push_getBgNotificationData: (CDVInvokedUrlCommand *)command;
- (void) push_getForegroundNotificationData: (CDVInvokedUrlCommand *)command;

- (void) initTyDeviceWithDevId: (NSString *)devId;
- (void) destroyTyDevice;
- (void) saveBgNotificationData: (NSDictionary *) userData;
- (void) sendForegroundNotification: (NSDictionary *)userData;

+ (Tuyacordovaplugin *) tuyacordovaplugin;

@property (nonatomic) NSString* latestCallbackIdForConfiguration;
@property (nonatomic) NSString* latestCallbackIdForDpsUpdate;
@property (nonatomic) NSString* latestCallbackIdForSignalStrength;
@property (nonatomic) NSString* latestCallbackIdForForegroundNotification;
@property (nonatomic) NSDictionary* bgNotificationData;

@end
