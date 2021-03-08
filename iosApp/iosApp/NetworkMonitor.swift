import Foundation
import Network

protocol NetworkMonitorDelegate: AnyObject {
    
    func networkAvailablilityStatusChanged(isAvailable: Bool)
    
}

final class NetworkMonitor {
    
    // MARK: - Internal Static Properties
    
    static let shared = NetworkMonitor()
    
    // MARK: - Internal Properties
    
    weak var delegate: NetworkMonitorDelegate?
    
    // MARK: - Private Properties
    
    private let monitor = NWPathMonitor()
    
    // MARK: - Internal Methods
    
    func startMonitoring() {
        monitor.start(queue: .global())
        
        processPath(monitor.currentPath)

        monitor.pathUpdateHandler = { [weak self] path in
            self?.processPath(path)
        }
    }
    
    func stopMonitoring() {
        monitor.cancel()
    }
    
    // MARK: - Private Methods
    
    private func processPath(_ path: NWPath) {
        let isNetwotkAvailable = (path.status == .satisfied)
        delegate?.networkAvailablilityStatusChanged(isAvailable: isNetwotkAvailable)
    }
    
}
