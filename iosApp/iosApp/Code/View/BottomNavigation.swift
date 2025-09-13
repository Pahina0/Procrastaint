//
//  BottomNavigation.swift
//  iosApp
//
//  Created by Yi Chen on 6/11/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import shared

struct BottomNavigation: View {
    var taskRepository: TaskRepository

    var body: some View {
        ZStack{
            TabView{
                CalendarView(taskRepository: taskRepository)
                    .tabItem { Label("Schedule", systemImage: "calendar") }
                TodayViews(taskRepository: taskRepository)
                    .tabItem { Label("Today", systemImage: "list.bullet.clipboard") }
                LibraryViews()
                    .tabItem{ Label("Library", systemImage: "building.columns")}
                SettingViews()
                    .tabItem { Label("Setting", systemImage: "gear") }
            }
            
            
        }
        
    }
}

#Preview {
    BottomNavigation(taskRepository: DependencyProvider.shared.taskRepository)
}
