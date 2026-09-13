import EMMA_iOS

class EmmaInstallAttributionDelegate: NSObject, EMMAInstallAttributionDelegate {
    var resolve: (Any?) -> Void
    var reject: (String, String, NSError?) -> Void

    init(resolve: @escaping (Any?) -> Void, reject: @escaping (String, String, NSError?) -> Void) {
        self.resolve = resolve
        self.reject = reject
    }

    func onAttributionReceived(_ attribution: EMMAInstallAttribution!) {
        EmmaReactNativeManager.installAttributionDelegate = nil
        resolve(EmmaSerializer.installAttributionToDictionary(attribution))
    }
}
