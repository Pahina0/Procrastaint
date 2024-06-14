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
        TabView{
            ContentView()
                .tabItem { Label("Schedule", systemImage: "calendar") }
        }
    }
}

#Preview {
    BottomNavigation()
}
