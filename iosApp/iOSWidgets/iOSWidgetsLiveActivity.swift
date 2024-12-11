//
//  iOSWidgetsLiveActivity.swift
//  iOSWidgets
//
//  Created by Yi Chen on 11/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import ActivityKit
import WidgetKit
import SwiftUI

struct iOSWidgetsAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        // Dynamic stateful properties about your activity go here!
        var emoji: String
    }

    // Fixed non-changing properties about your activity go here!
    var name: String
}

struct iOSWidgetsLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: iOSWidgetsAttributes.self) { context in
            // Lock screen/banner UI goes here
            VStack {
                Text("Hello \(context.state.emoji)")
            }
            .activityBackgroundTint(Color.cyan)
            .activitySystemActionForegroundColor(Color.black)

        } dynamicIsland: { context in
            DynamicIsland {
                // Expanded UI goes here.  Compose the expanded UI through
                // various regions, like leading/trailing/center/bottom
                DynamicIslandExpandedRegion(.leading) {
                    Text("Leading")
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text("Trailing")
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text("Bottom \(context.state.emoji)")
                    // more content
                }
            } compactLeading: {
                Text("L")
            } compactTrailing: {
                Text("T \(context.state.emoji)")
            } minimal: {
                Text(context.state.emoji)
            }
            .widgetURL(URL(string: "http://www.apple.com"))
            .keylineTint(Color.red)
        }
    }
}

extension iOSWidgetsAttributes {
    fileprivate static var preview: iOSWidgetsAttributes {
        iOSWidgetsAttributes(name: "World")
    }
}

extension iOSWidgetsAttributes.ContentState {
    fileprivate static var smiley: iOSWidgetsAttributes.ContentState {
        iOSWidgetsAttributes.ContentState(emoji: "😀")
     }
     
     fileprivate static var starEyes: iOSWidgetsAttributes.ContentState {
         iOSWidgetsAttributes.ContentState(emoji: "🤩")
     }
}

#Preview("Notification", as: .content, using: iOSWidgetsAttributes.preview) {
   iOSWidgetsLiveActivity()
} contentStates: {
    iOSWidgetsAttributes.ContentState.smiley
    iOSWidgetsAttributes.ContentState.starEyes
}
