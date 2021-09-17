    var exec = function exec(method, params) {
        return new Promise(function (resolve, reject) {
          return cordova.exec(resolve, reject, pluginName, method, params);
        });
    };
    
    const pluginName = 'Tuyacordovaplugin';
    
    var Home = {
        createHome: function createHome(){
            return exec('home_createHome',[name,geoName,rooms]);
        },
        listHomes: function listHomes(){
            return exec('home_listHomes',[]);
        },
        getCurrentHome : function getCurrentHome(){
    
        },
        setCurrentHome : function setCurrentHome(){
            
        },
        listDevices: function listDevices(homeId){
            return exec('home_listDevices',[homeId]);
        },
        initNotifications: function initNotifications(pushToken) {
            return exec('home_initNotifications', [pushToken]);
        }
    
    }
    
    var User = {
        requestRegister : function requestRegister({countryCode, email, password}){
            return exec('user_requestRegister', [countryCode, email, password]);
        },
        register: function register({countryCode, email, password, otp}) {
            return exec('user_register', [countryCode, email, password, otp])
        },
        loginOrRegitserWithUID : function loginOrRegitserWithUID({countryCode, uid, password}){
            return exec('user_loginOrRegitserWithUID',[countryCode, uid,password])
        },
        isLoggedIn: function isLoggin(){
            return exec('user_isLoggedIn', []);
        },
        logout: function logout(){
            return exec('user_logout', []);
        }
    }
    
    var Networking = {
        smartCameraConfiguration: function smartCameraConfiguration({ssid,pass,homeId}, successCallback, errorCallback){
            return cordova.exec(successCallback, errorCallback, pluginName, 'network_smartCameraConfiguration', [ssid,pass,homeId]);
        },
        stopCameraConfiguration: function smartCameraConfiguration(successCallback, errorCallback){
            return cordova.exec(successCallback, errorCallback, pluginName, 'network_stopCameraConfiguration', []);
        }
    }
    
    var Utils = {
        getDeviceData: function getDeviceData({devId,homeId}){
            return exec('device_data', [devId,homeId]);
        },
        setDeviceDps: function setDeviceDps({devId,dps}){
         return exec('setDPs',[devId,dps]);
        },
        renameDevice: function renameDevice({devId,deviceName}){
            return exec('renameDevice',[devId,deviceName]);
        },
        removeDevice: function removeDevice({devId}){
            return exec('removeDevice',[devId])
        },
        signalStrength: function signalStrength({devId}){
            return exec('signalStrength',[devId]);
        }
    }
    
    var IPC = {
        startCameraLivePlay : function startCameraLivePlay({devId, bgColor, primaryColor, itemBgColor, textColor1, textColor2, dpConfig}, successCallback, errorCallback){
            return cordova.exec(successCallback, errorCallback, pluginName, 'ipc_startCameraLivePlay', [devId, bgColor, primaryColor, itemBgColor, textColor1, textColor2, dpConfig]);
        },
        getImagesOnMotionDetection: function getImagesOnMotionDetection({devId, startTime, endTime, limit, offset}, successCallback, errorCallback){
            return cordova.exec(successCallback, errorCallback, pluginName, 'ipc_getImagesOnMotionDetection', [devId, startTime, endTime, limit, offset]);
        }
    }
    
    var Lock = {
    
    }

    var Push = {
        getBgNotificationData: function getBgNotificationData() {
            return exec('push_getBgNotificationData', []);
        },
        getForegroundNotificationData: function getForegroundNotificationData(successCallback, errorCallback) {
            return cordova.exec(successCallback, errorCallback, pluginName, 'push_getForegroundNotificationData', []);
        }
    }
    
    var Tuya = {
        Home,
        User,
        Networking,
        Utils,
        IPC,
        Lock,
        Push
    }
    
    module.exports = Tuya;
    
