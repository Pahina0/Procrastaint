//
//  ThemeView.swift
//  iosApp
//
//  Created by Yi Chen on 12/10/24.
//

import SwiftUI

struct ThemeView: View {
    @State private var selectedTheme = "Dark"
    let themes = ["Light", "Dark", "System"]
    
    var body: some View {
        Form {
            Section(header: Text("Theme Appearance")) {
                Picker("App Theme", selection: $selectedTheme) {
                    ForEach(themes, id: \.self) { theme in
                        Text(theme)
                    }
                }
                .pickerStyle(SegmentedPickerStyle())
            }
            
            Section(header: Text("Color Scheme")) {
                ColorSchemeSelector()
            }
        }
        .navigationTitle("Theme")
    }
}

struct ColorSchemeSelector: View {
    let accentColors = [
        ("Blue", Color.blue),
        ("Green", Color.green),
        ("Purple", Color.purple),
        ("Red", Color.red),
        ("Orange", Color.orange)
    ]
    
    @State private var selectedAccentColor = "Blue"
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack {
                ForEach(accentColors, id: \.0) { color in
                    VStack {
                        Circle()
                            .fill(color.1)
                            .frame(width: 50, height: 50)
                            .overlay(
                                Circle()
                                    .stroke(selectedAccentColor == color.0 ? Color.black : Color.clear, lineWidth: 3)
                            )
                            .onTapGesture {
                                selectedAccentColor = color.0
                            }
                        
                        Text(color.0)
                            .font(.caption)
                    }
                }
            }
        }
    }
}

#Preview {
    NavigationView {
        ThemeView()
    }
}
