//
//  BottomNavigation.swift
//  iosApp
//
//  Created by Yi Chen on 6/11/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct BottomNavigation: View {
    var body: some View {
        ZStack{
            TabView{
                CalendarView()
                    .tabItem { Label("Schedule", systemImage: "calendar") }
                TodayViews()
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
    BottomNavigation()
}
