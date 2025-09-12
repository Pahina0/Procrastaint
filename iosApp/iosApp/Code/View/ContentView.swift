import SwiftUI
import shared

struct ContentView: View {
    var taskRepository: TaskRepository
    
    var body: some View {
        BottomNavigation(taskRepository: taskRepository)
    }
}
