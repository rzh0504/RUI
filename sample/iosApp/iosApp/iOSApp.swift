import UIKit
import SampleShared

/// Compose's iOS content cannot flip the system bars: the view controller
/// level owns the style there and the Obj-C binding keeps the hook final
/// even from Kotlin, and SwiftUI has no public status-bar style hook to
/// lean on either. So the app is hosted by this plain UIKit root: it embeds
/// the Compose controller as its child and answers the system's status bar
/// queries from the look Rui publishes through `StatusBarBridge`.
final class StatusStyleHostController: UIViewController {
    private let compose: UIViewController
    private var stylePoll: Timer?

    init(compose: UIViewController) {
        self.compose = compose
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("The host is created programmatically")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(compose)
        view.addSubview(compose.view)
        compose.didMove(toParent: self)
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        compose.view.frame = view.bounds
    }

    // The request changes while Compose pages join and leave, and nothing on
    // the Compose side can push the change into UIKit, so the host polls on
    // a light cadence; refreshing an unchanged style is a no-op for the
    // system.
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        stylePoll?.invalidate()
        let poll = Timer(timeInterval: 0.25, repeats: true) { [weak self] _ in
            self?.setNeedsStatusBarAppearanceUpdate()
        }
        RunLoop.main.add(poll, forMode: .common)
        stylePoll = poll
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        stylePoll?.invalidate()
        stylePoll = nil
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        switch StatusBarBridge.shared.request {
        case 0:
            return .lightContent
        default:
            return .darkContent
        }
    }
}

@main
class AppDelegate: NSObject, UIApplicationDelegate {
    // The generated Info.plist carries no scene manifest, so the classic
    // window lifecycle applies and this window is the app's root scene.
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.rootViewController = StatusStyleHostController(
            compose: MainViewControllerKt.MainViewController()
        )
        window.makeKeyAndVisible()
        self.window = window
        return true
    }
}
