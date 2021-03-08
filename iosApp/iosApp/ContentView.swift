import SwiftUI
import common

func greet() -> String {
    return "test" // Greeting().greeting()
}

struct ContentView: View {
    
    @ObservedObject var viewModel = ContentViewModel()
    
    var body: some View {
        // Text(greet())
        Text(self.viewModel.isNetwotkAvailable ? "HAS NETWORK" : "NO INTERNET")
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
