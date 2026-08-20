import EmmaSdk, {
  IN_APP_TYPE,
  LoginRegisterUserParams,
  NativeAd,
  PERMISSION_STATUS,
  PurchaseRequest,
  StartSessionParams,
} from 'emma-react-native-sdk';
import React, { useEffect, useState } from 'react';
import {
  Linking,
  Platform,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  View,
  useColorScheme,
} from 'react-native';
import { Picker } from '@react-native-picker/picker';

import Button from './components/Button';
import Colors from './components/Colors';
import Header from './components/Header';
import LearnMoreLinks from './components/LearnMoreLinks';
import NativeAdView from './components/NativeAdView';
import Section from './components/Section';

/**
 * Sample React Native App with EMMA SDK
 */
enum SESSION_STATE {
  NONE,
  STARTED,
  FAILED,
  STARTING,
}

const startSessionParams: StartSessionParams = {
  sessionKey: '',
  isDebug: true,
};

const userParams: LoginRegisterUserParams = {
  userId: '2002',
};

const App = () => {
  // Internal states
  const [started, setStarted] = useState(SESSION_STATE.NONE);
  const [notificationEnabled, setNotificationsEnabled] = useState(false);
  const [notificationPermissionStatus, setNotificationPermissionStatus] = useState<PERMISSION_STATUS | null>(
    null
  );
  const [logged, setLogged] = useState(false);
  const [registered, setRegistered] = useState(false);
  const [nativeAds, setNativeAds] = useState<NativeAd[] | null>(null);
  const [deeplink, setDeeplink] = useState<string | null>(null);
  const [hasOrder, setHasOrder] = useState<boolean>(false);
  const [hasProducts, setHasProducts] = useState<boolean>(false);
  const [trackedOrder, setTrackedOrder] = useState<boolean>(false);
  const [selectedLanguage, setSelectedLanguage] = useState('Spanish');

  // Tweak colors
  const isDarkMode = useColorScheme() === 'dark';
  const styles = getStyles(isDarkMode);

  // Banner is only supported on Android devices
  const isAndroid = Platform.OS === 'android';
  const isIos = Platform.OS === 'ios';

  // Descriptive feedback
  let startedDesc;
  switch (started) {
    case SESSION_STATE.STARTED:
      startedDesc = 'Session started';
      break;
    case SESSION_STATE.STARTING:
      startedDesc = 'Starting session...';
      break;
    case SESSION_STATE.FAILED:
      startedDesc = 'Error logged';
      break;
    default:
      startedDesc = '';
  }

  // Interaction handlers
  const handleStartSession = async () => {
    console.log('Starting session...');
    setStarted(SESSION_STATE.STARTING);
    try {
      await EmmaSdk.startSession(startSessionParams);
      EmmaSdk.startPush({
        classToOpen: 'com.emmareactnativeexample.MainActivity',
        iconResource: 'notification_icon',
      });
      setStarted(SESSION_STATE.STARTED);
      console.log('Session started on', Platform.OS);
    } catch (err) {
      setStarted(SESSION_STATE.FAILED);
      console.error('Session failed to start', err);
    }

    const areNotificationsEnabled = await EmmaSdk.areNotificationsEnabled();
    setNotificationsEnabled(areNotificationsEnabled);
  };

  const requestNotificationsPermission = async () => {
    try {
      const permissionStatus = await EmmaSdk.requestNotificationPermission();
      setNotificationPermissionStatus(permissionStatus);
    } catch (e) {
      setNotificationPermissionStatus(null);
    }

    const areNotificationsEnabled = await EmmaSdk.areNotificationsEnabled();
    setNotificationsEnabled(areNotificationsEnabled);
  };

  const permissionStatusToString = (permissionStatus: PERMISSION_STATUS | null) => {
    switch (permissionStatus) {
      case PERMISSION_STATUS.GRANTED:
        return 'GRANTED';
      case PERMISSION_STATUS.DENIED:
        return 'DENIED';
      case PERMISSION_STATUS.SHOULD_PERMISSION_RATIONALE:
        return 'SHOULD_PERMISSION_RATIONALE';
      case PERMISSION_STATUS.UNSUPPORTED:
        return 'UNSUPPORTED';
      default:
        return '';
    }
  };

  const handleLoginUser = () => {
    console.log('Login user');
    EmmaSdk.loginUser(userParams);
    setLogged(true);
  };

  const handleRegisterUser = () => {
    console.log('Register user');
    EmmaSdk.registerUser(userParams);
    setRegistered(true);
  };

  const handleInAppMessage = (type: IN_APP_TYPE, templateId?: string) => async () => {
    try {
      const result = await EmmaSdk.inAppMessage({ type, templateId });
      setNativeAds(result);
      console.log(`InApp ${type} message`, result);
    } catch (err) {
      console.error('InApp message error', err);
    }
  };

  const handleSetLanguage = () => {
    let code = 'es';
    switch (selectedLanguage) {
      case 'English':
        code = 'en';
        break;
      case 'French':
        code = 'fr';
        break;
      case 'German':
        code = 'de';
        break;
      default:
        code = 'es';
    }
    console.log('Language selected:', code);
    EmmaSdk.setUserLanguage(code);
  };

  const handleGetSdkVersion = async () => {
    const version = await EmmaSdk.getSdkVersion();
    console.log('EMMA SDK version:', version);
  };

  const handleTrackPurchase = async () => {
    try {
      const purchase: PurchaseRequest = {
        id: 'EMMA',
        totalPrice: 24.12,
        customerId: '2002',
        products: [{ id: 'SDK', name: 'SDK', price: 12.06, qty: 2, extras: { ReactNative: 'working' } }],
      };
      await EmmaSdk.trackPurchase(purchase);
      setHasOrder(false);
      setHasProducts(false);
      setTrackedOrder(false);
      console.log('Purchase tracked');
    } catch (err) {
      console.error('Track purchase error', err);
    }
  };

  // Listen to deeplink requests
  Linking.addEventListener('url', ({ url }) => setDeeplink(url));

  // Detect application launchings from a deeplink
  const handleInitialDeeplink = () =>
    Linking.getInitialURL()
      .then((url) => setDeeplink(url || null))
      .catch(() => setDeeplink(null));

  /**
   * Start session on App mount, it is needed for everything else.
   * Remove this effect if you want to start session manually or from another event.
   */
  useEffect(() => {
    handleStartSession();
    handleInitialDeeplink();
  }, []);

  // Render
  return (
    <SafeAreaView style={styles.mainView}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView contentInsetAdjustmentBehavior="automatic" style={styles.mainView}>
        <Header />
        <View style={styles.scrollView}>
          <Section title="SDK Version" />
          <View style={styles.buttonSection}>
            <Button onPress={handleGetSdkVersion} title="Get SDK Version" />
          </View>
          <Section title="Deeplink" subtitle={deeplink ? 'Deeplink received' : 'No deeplink'}>
            {deeplink ? deeplink : 'Received deeplink will be displayed here.'}
          </Section>
          <Section title="Session" subtitle={startedDesc}>
            Session is required. Usually, it should be triggered when the App is ready.
          </Section>
          <View style={styles.buttonSection}>
            <Button
              onPress={handleStartSession}
              title="Start Session"
              disabled={started === SESSION_STATE.STARTED}
            />
          </View>
          <View
            style={started !== SESSION_STATE.STARTED && styles.veil}
            pointerEvents={started === SESSION_STATE.STARTED ? 'auto' : 'none'}
          >
            {isAndroid && (
              <View>
                <Section
                  title="Notifications Permission"
                  subtitle={notificationEnabled ? 'Enabled' : 'Disabled'}
                >
                  Request notifications permissions for Android 13.{'\n'}
                  Permission status: {permissionStatusToString(notificationPermissionStatus)}
                </Section>
                <View style={styles.buttonSection}>
                  <Button onPress={requestNotificationsPermission} title="Request notifications permission" />
                </View>
              </View>
            )}
            <Section title="Register User" subtitle={registered ? 'User registered' : ''} />
            <View style={styles.buttonSection}>
              <Button onPress={handleRegisterUser} title="Register User" disabled={registered} />
            </View>
            <Section title="Log in User" subtitle={logged ? 'User logged in' : ''} />
            <View style={styles.buttonSection}>
              <Button onPress={handleLoginUser} title="Log in User" disabled={logged} />
            </View>
            <Section title="Events and Extras">These buttons do not have UI feedback.</Section>
            <View style={styles.buttonSection}>
              <Button
                onPress={() => {
                  EmmaSdk.trackEvent({
                    eventToken: '7b358954cf16bc2b7830bb5307f80f96',
                    eventAttributes: { ReactNative: 'true' },
                  });
                  // Sends conversionValue to SKAdNetwork postback
                  EmmaSdk.updateConversionValueSkad4({
                    coarseValue: 'high',
                    conversionValue: 3,
                    lockWindow: false,
                  });
                }}
                title="Track event"
              />
              <Button
                onPress={() => {
                  EmmaSdk.trackUserExtraInfo({ userTags: { TAG: 'EMMA_EXAMPLE' } });
                }}
                title="Add user tag 'TAG'"
              />
            </View>
            <Section title="In-App Communication">Try our in-app communications:</Section>
            <View style={styles.buttonSection}>
              <Button onPress={handleInAppMessage(IN_APP_TYPE.ADBALL)} title="Show AdBall" />
              <Button onPress={handleInAppMessage(IN_APP_TYPE.STARTVIEW)} title="Show StartView" />
              <Button onPress={handleInAppMessage(IN_APP_TYPE.STRIP)} title="Show Strip" />
              {isAndroid && <Button onPress={handleInAppMessage(IN_APP_TYPE.BANNER)} title="Show Banner" />}
              <Button
                onPress={handleInAppMessage(IN_APP_TYPE.NATIVE_AD, 'batch-template2')}
                title="Show Native Ad"
              />
            </View>
          </View>
          <Section title="Orders and Products" subtitle={hasOrder ? 'Order started' : ''}>
            Track your orders.
          </Section>
          <View style={styles.buttonSection}>
            <Button
              onPress={() => {
                EmmaSdk.startOrder({
                  orderId: 'EMMA',
                  totalPrice: 24.12,
                  customerId: 'EMMA',
                  currencyCode: 'USD',
                });
                setHasOrder(true);
                setTrackedOrder(false);
              }}
              title="Start order"
              disabled={trackedOrder}
            />
            <Button
              onPress={() => {
                EmmaSdk.addProduct({
                  productId: 'SDK',
                  productName: 'SDK',
                  quantity: 2,
                  price: 12.23,
                  extras: { ReactNative: 'working' },
                });
                setHasProducts(true);
              }}
              title="Add product"
              disabled={!hasOrder}
            />
            <Button
              onPress={() => {
                EmmaSdk.trackOrder();
                setHasOrder(false);
                setTrackedOrder(true);
              }}
              title="Track order"
              disabled={!hasOrder || !hasProducts}
            />
            <Button
              onPress={handleTrackPurchase}
              title="Track purchase"
            />
          </View>
          <NativeAdView nativeAds={nativeAds} />
          {isIos && (
            <View>
              <Section title="IDFA and iOS">Request tracking with IDFA for iOS devices.</Section>
              <View style={styles.buttonSection}>
                <Button onPress={() => EmmaSdk.requestTrackingWithIdfa()} title="Request IDFA tracking" />
              </View>
            </View>
          )}
          <View>
            <Section title="Language">
              Change the language to receive notifications, communications and more in this language.
            </Section>
            <Picker
              selectedValue={selectedLanguage}
              onValueChange={(itemValue) => setSelectedLanguage(itemValue)}
              style={styles.picker}
            >
              <Picker.Item label="Spanish" value="Spanish" />
              <Picker.Item label="English" value="English" />
              <Picker.Item label="French" value="French" />
              <Picker.Item label="German" value="German" />
            </Picker>
            <Button title="Set language" onPress={handleSetLanguage} />
          </View>
          <Section title="Learn More">Read the docs to discover what to do next:</Section>
          <LearnMoreLinks />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const getStyles = (isDarkMode: boolean) =>
  StyleSheet.create({
    mainView: {
      backgroundColor: Colors.primary,
    },
    scrollView: {
      paddingHorizontal: 15,
      backgroundColor: isDarkMode ? Colors.black : Colors.white,
    },
    highlight: {
      fontWeight: '700',
    },
    buttonSection: {
      flexDirection: 'row',
      justifyContent: 'space-evenly',
      flexWrap: 'wrap',
    },
    veil: {
      opacity: 0.3,
    },
    button: {
      color: isDarkMode ? Colors.primaryBright : Colors.primary,
    },
    picker: {
      width: '100%',
    },
  });

export default App;
