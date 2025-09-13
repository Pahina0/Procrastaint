import SwiftUI
import shared

@main
struct iOSApp: App {
    private let db: ProcrastaintDatabase
    private let taskRepository: TaskRepository

    init() {
        AppModule_iosKt.initialize()
        db = DependencyProvider.shared.database
        taskRepository = DependencyProvider.shared.taskRepository
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView(taskRepository: taskRepository)
        }
    }
}

