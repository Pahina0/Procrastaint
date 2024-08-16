//
//  AddButtonView.swift
//  iosApp
//
//  Created by Yi Chen on 8/12/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI

struct AddButtonView: View {
    
    @State private var isSheetPresented = false
    
    var body: some View {
        
        VStack {
            Spacer() // Pushes the content below to the bottom
            HStack{
                Spacer()
                Button(action: {
                    // Action to perform when the button is pressed
                    isSheetPresented.toggle()
                    print("Add button tapped")
                }) {
                    Label("Add", systemImage: "plus.app")                     .font(.headline)
                        .foregroundColor(.white)
                        .padding()
                        .background(Color.blue)
                        .cornerRadius(10)
                        .padding(.horizontal)
                }
                .padding(.bottom, 20)
                .padding(.trailing, 10)
                .sheet(isPresented: $isSheetPresented, content: {
                    BottomSheetView().presentationDetents([.fraction(0.38)])
                })
                
            }

        }

        

    }
}

#Preview {
    AddButtonView()
}
