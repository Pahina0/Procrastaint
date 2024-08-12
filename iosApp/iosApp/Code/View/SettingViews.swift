//
//  SettingViews.swift
//  iosApp
//
//  Created by Yi Chen on 6/18/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct SettingViews: View {
    var body: some View {
        NavigationView {
            List {
                // Account Section
                Section {
                    NavigationLink{
                        
                    }label: {
                        Label("Account", systemImage: "person.circle")
                    }
                    NavigationLink{
                        
                    }label: {
                        Label("General", systemImage: "gearshape")
                    }
                    NavigationLink{
                        
                    }label: {
                        HStack {
                            Label("Calendar", systemImage: "calendar")
                            
                        }
                    }
                }
                
                // Personalization Section
                Section(header: Text("Personalization").foregroundColor(.blue)) {
                    NavigationLink{
                        
                    }label: {
                        HStack {
                            Label("Theme", systemImage: "paintpalette")
                            Spacer()
                            Text("Dark")
                                .foregroundColor(.gray)
                        }
                    }
                    NavigationLink{
                        
                    }label:{
                        HStack {
                            Label("App icon", systemImage: "square.grid.2x2")
                            Spacer()
                            Text("Todoist")
                                .foregroundColor(.gray)
                        }
                    }
                    NavigationLink{
                        
                    }label: {
                        Label("Navigation bar", systemImage: "rectangle.grid.1x2")
                    }
                    NavigationLink{
                        
                    }label:{
                        Label("Navigation menu", systemImage: "list.bullet")
                    }
                    NavigationLink{
                        
                    }label:{
                        Label("Quick Add", systemImage: "plus.square")
                    }
                }
                
                // Productivity Section
                Section(header: Text("Productivity").foregroundColor(.blue)) {
                    NavigationLink{
                        
                    }label:{
                        Label("Productivity", systemImage: "chart.bar")
                    }
                    NavigationLink{
                        
                    }label: {
                        HStack {
                            Label("Reminders", systemImage: "alarm")
                        }
                    }
                    NavigationLink{
                        
                    }label: {
                        Label("Notifications", systemImage: "bell")
                    }
                }
            }
            .navigationTitle("Settings")
            .listStyle(InsetGroupedListStyle())
        }
    }
}



#Preview {
    SettingViews()
}
