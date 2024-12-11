//
//  GeneralView.swift
//  iosApp
//
//  Created by Yi Chen on 12/10/24.
//

import SwiftUI

struct GeneralView: View {
    @State private var startOfWeek = "Monday"
    @State private var firstDayOptions = ["Sunday", "Monday"]
    
    @State private var timeFormat = "12-hour"
    @State private var timeFormatOptions = ["12-hour", "24-hour"]
    
    @State private var language = "English"
    @State private var languageOptions = ["English", "Spanish", "French", "German"]
    
    var body: some View {
        Form {
            Section(header: Text("Regional Settings")) {
                Picker("First Day of Week", selection: $startOfWeek) {
                    ForEach(firstDayOptions, id: \.self) { day in
                        Text(day)
                    }
                }
                
                Picker("Time Format", selection: $timeFormat) {
                    ForEach(timeFormatOptions, id: \.self) { format in
                        Text(format)
                    }
                }
                
                Picker("Language", selection: $language) {
                    ForEach(languageOptions, id: \.self) { lang in
                        Text(lang)
                    }
                }
            }
            
            Section(header: Text("Data & Privacy")) {
                NavigationLink(destination: PrivacySettingsView()) {
                    Label("Privacy Settings", systemImage: "lock.shield")
                }
                
                NavigationLink(destination: DataManagementView()) {
                    Label("Data Management", systemImage: "doc.on.doc")
                }
            }
        }
        .navigationTitle("General")
    }
}

struct PrivacySettingsView: View {
    @State private var isAnalyticsEnabled = true
    @State private var isCrashReportingEnabled = true
    
    var body: some View {
        Form {
            Section(header: Text("Analytics")) {
                Toggle("Send Anonymous Usage Data", isOn: $isAnalyticsEnabled)
                Toggle("Crash Reporting", isOn: $isCrashReportingEnabled)
            }
        }
        .navigationTitle("Privacy Settings")
    }
}

struct DataManagementView: View {
    var body: some View {
        Form {
            Section(header: Text("Data Export")) {
                Button(action: {
                    // TODO: Implement data export
                }) {
                    Label("Export All Data", systemImage: "square.and.arrow.up")
                }
            }
            
            Section(header: Text("Data Reset")) {
                Button(action: {
                    // TODO: Implement data reset
                }) {
                    Label("Reset App Data", systemImage: "trash")
                        .foregroundColor(.red)
                }
            }
        }
        .navigationTitle("Data Management")
    }
}

#Preview {
    NavigationView {
        GeneralView()
    }
}
