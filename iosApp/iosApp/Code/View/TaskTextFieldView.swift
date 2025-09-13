
import SwiftUI
import shared

public struct TaskTextFieldView: View {
    @Binding public var text: String
    public var suggestions: [TaskTag]
    public var onValueChange: (String) -> Void
    public var onCurrentWordChange: (String) -> Void
    
    public init(text: Binding<String>, suggestions: [TaskTag], onValueChange: @escaping (String) -> Void, onCurrentWordChange: @escaping (String) -> Void) {
        self._text = text
        self.suggestions = suggestions
        self.onValueChange = onValueChange
        self.onCurrentWordChange = onCurrentWordChange
    }
    
    public var body: some View {
        VStack {
            TextField("What's on your mind?", text: $text)
                .onChange(of: text) { newValue in
                    onValueChange(newValue)
                    let currentWord = getCurrentWord(text: newValue)
                    onCurrentWordChange(currentWord)
                }
                .padding()
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.gray, lineWidth: 1))
            
            if !suggestions.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack {
                        ForEach(suggestions, id: \.self) { suggestion in
                            Button(action: {
                                let newText = replaceCurrentWord(text: text, with: suggestion.generateTag())
                                text = newText
                            }) {
                                Text(suggestion.title)
                                    .padding(8)
                                    .background(RoundedRectangle(cornerRadius: 10).fill(Color.gray.opacity(0.2)))
                            }
                        }
                    }
                }
            }
        }
    }
    
    private func getCurrentWord(text: String) -> String {
        let cursorPosition = text.count
        let textAsNSString = text as NSString
        let range = textAsNSString.rangeOfComposedCharacterSequence(at: cursorPosition - 1)
        let word = textAsNSString.substring(with: range)
        return word
    }
    
    private func replaceCurrentWord(text: String, with replacement: String) -> String {
        // This is a simplified implementation. A more robust solution would be needed for a production app.
        let words = text.split(separator: " ")
        if let lastWord = words.last {
            return text.replacingOccurrences(of: String(lastWord), with: replacement)
        }
        return text
    }
}
