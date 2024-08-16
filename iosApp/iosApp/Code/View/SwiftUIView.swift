//
//  SwiftUIView.swift
//  iosApp
//
//  Created by Yi Chen on 8/2/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct SwiftUIView: View {
    @State private var searchText = ""
    @State private var recentTags = ["School", "Work", "Personal", "Today", "Upcoming"]
    @State private var tasks = [
        "Complete Assignment",
        "Grocery Shopping",
        
    ]
    
    var body: some View {
        VStack {
            HStack{
                Text("Search")
                    .font(.title)
                    .padding([ .leading])
                Spacer()
            }
            
            SearchBar(text: $searchText)
                            
            if searchText.isEmpty {
                // Display Recent Tags
                HStack{
                    Text("Recently visited")
                        .font(.headline)
                        .padding([.top, .leading])
                    Spacer()
                }
                
                List {
                    ForEach(recentTags, id: \.self) { tag in
                        HStack {
                            Image(systemName: "tag")
                                .foregroundColor(.gray)
                            Text(tag)
                        }
                    }
                }
                .listStyle(PlainListStyle())
            } else {
                // Display Search Results
                NavigationStack{
                    List {
                        ForEach(filteredTasks, id: \.self) { task in
                            HStack {
                                Image(systemName: "doc.text.magnifyingglass")
                                    .foregroundColor(.gray)
                                Text(task)
                            }
                        }
                    }
                    .listStyle(PlainListStyle())
                }
                
            }
        }

        
    }
    
    var filteredTasks: [String] {
            if searchText.isEmpty {
                return []
            } else {
                return tasks.filter { $0.localizedCaseInsensitiveContains(searchText) }
            }
    }
    
    

}

struct Bar: View {
    @Binding var text: String
    
    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)
            TextField("Tasks, projects, and more", text: $text)
                .foregroundColor(.primary)
                .padding(7)
                .padding(.horizontal, 25)
                .background(Color(.systemGray6))
                .cornerRadius(8)
                .overlay(
                    HStack {
                        Spacer()
                        if !text.isEmpty {
                            Button(action: {
                                self.text = ""
                            }) {
                                Image(systemName: "multiply.circle.fill")
                                    .foregroundColor(.gray)
                                    .padding(.trailing, 8)
                            }
                        }
                    }
                )
        }
        .padding(.horizontal)
    }
}


#Preview {
    SwiftUIView()
}
