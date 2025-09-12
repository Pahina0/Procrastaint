import SwiftUI
import shared

public struct BottomSheetView: View {
    @StateObject private var viewModel: BottomSheetViewModel
    
    public init(db: TaskRepository) {
        _viewModel = StateObject(wrappedValue: BottomSheetViewModel(db: db))
    }
    
    public var body: some View {
        VStack {
            TaskTextFieldView(
                text: $viewModel.task,
                suggestions: viewModel.getTagsStarting(title: viewModel.task),
                onValueChange: { newValue in
                    viewModel.updateTask(title: newValue)
                },
                onCurrentWordChange: { _ in }
            )
            
            Divider()
            
            if !viewModel.task.isEmpty {
                ParsedTextView(text: viewModel.task, parsed: viewModel.parsed, viewing: viewModel.viewing)
            }
            
            TimeChipsView(parsedTime: viewModel.parsed?.times.first)
            
            ActionListView(
                viewNextParsed: { viewModel.viewNextParsed() },
                viewing: viewModel.viewing,
                currentViewingSize: viewModel.parsed?.times.count ?? 0
            )
            
            TextField("Description", text: $viewModel.description)
                .padding()
                .background(RoundedRectangle(cornerRadius: 10).stroke(Color.gray, lineWidth: 1))
            
            if case .edit(_) = viewModel.mode {
                Button(action: {
                    viewModel.deleteEditTask()
                }) {
                    Text("Delete task")
                        .foregroundColor(.red)
                }
            }
        }
        .padding()
    }
}
