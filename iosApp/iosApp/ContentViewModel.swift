import SwiftUI

final class ContentViewModel: ObservableObject {
    
    @Published var isNetwotkAvailable: Bool = false
    
    init() {
        NetworkMonitor.shared.delegate = self
        NetworkMonitor.shared.startMonitoring()
    }
    
}

extension ContentViewModel: NetworkMonitorDelegate {
    
    func networkAvailablilityStatusChanged(isAvailable: Bool) {
        DispatchQueue.main.async { [weak self] in
            self?.isNetwotkAvailable = isAvailable
        }
    }
    
}
