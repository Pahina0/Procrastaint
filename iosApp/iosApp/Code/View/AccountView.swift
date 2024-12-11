//
//  AccountView.swift
//  iosApp
//
//  Created by Yi Chen on 12/10/24.
//

import SwiftUI

struct AccountView: View {
    @State private var username = ""
    @State private var email = ""
    @State private var isEditingProfile = false
    
    var body: some View {
        Form {
            Section(header: Text("Profile Details")) {
                TextField("Username", text: $username)
                TextField("Email", text: $email)
                
                Button(action: {
                    // TODO: Implement profile update logic
                    isEditingProfile = true
                }) {
                    Text("Update Profile")
                }
            }
            
            Section(header: Text("Account Management")) {
                NavigationLink(destination: ChangePasswordView()) {
                    Label("Change Password", systemImage: "lock")
                }
                
                Button(action: {
                    // TODO: Implement logout logic
                }) {
                    Label("Logout", systemImage: "rectangle.portrait.and.arrow.right")
                        .foregroundColor(.red)
                }
            }
        }
        .navigationTitle("Account")
    }
}

struct ChangePasswordView: View {
    @State private var currentPassword = ""
    @State private var newPassword = ""
    @State private var confirmPassword = ""
    
    var body: some View {
        Form {
            Section(header: Text("Change Password")) {
                SecureField("Current Password", text: $currentPassword)
                SecureField("New Password", text: $newPassword)
                SecureField("Confirm New Password", text: $confirmPassword)
                
                Button(action: {
                    // TODO: Implement password change logic
                    // Add validation for password change
                }) {
                    Text("Change Password")
                }
            }
        }
        .navigationTitle("Change Password")
    }
}

#Preview {
    NavigationView {
        AccountView()
    }
}
