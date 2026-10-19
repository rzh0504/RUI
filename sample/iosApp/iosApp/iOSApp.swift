import SwiftUI
import SampleShared

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            StatusStyleHost()
        }
    }
}

/// Compose's iOS content cannot flip the system bars: the view controller
/// level owns the style there and the Obj-C binding keeps the hook final.
/// This host instead reads the look Rui's content publishes through
/// `StatusBarBridge` and applies it with the `statusBarStyle` modifier.
/// `TimelineView` ticks on a light cadence so the modifier re-evaluates when
/// the request changes (a page override joining or leaving); with no call
/// composed the sample falls back to dark content. The bridge publishes one
/// process-wide look: this sample hosts exactly one Compose composition.
struct StatusStyleHost: View {
    var body: some View {
        TimelineView(.animation(minimumInterval: 0.25)) { _ in
            ContentView()
                .statusBarStyle(statusStyle())
                .ignoresSafeArea()
        }
    }

    private func statusStyle() -> UIStatusBarStyle {
        switch StatusBarBridge.shared.request {
        case 0: .lightContent
        default: .darkContent
        }
    }
}
