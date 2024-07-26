//
//  testingLiveActivity.swift
//  testing
//
//  Created by Yi Chen on 7/26/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import ActivityKit
import WidgetKit
import SwiftUI

struct testingAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        // Dynamic stateful properties about your activity go here!
        var emoji: String
    }

    // Fixed non-changing properties about your activity go here!
    var name: String
}

struct testingLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: testingAttributes.self) { context in
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

extension testingAttributes {
    fileprivate static var preview: testingAttributes {
        testingAttributes(name: "World")
    }
}

extension testingAttributes.ContentState {
    fileprivate static var smiley: testingAttributes.ContentState {
        testingAttributes.ContentState(emoji: "😀")
     }
     
     fileprivate static var starEyes: testingAttributes.ContentState {
         testingAttributes.ContentState(emoji: "🤩")
     }
}

#Preview("Notification", as: .content, using: testingAttributes.preview) {
   testingLiveActivity()
} contentStates: {
    testingAttributes.ContentState.smiley
    testingAttributes.ContentState.starEyes
}
