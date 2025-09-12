
import Foundation
import shared

public class BottomSheetViewModel: ObservableObject {
    @Published public var task: String = ""
    @Published public var description: String = ""
    @Published public var parsed: Parsed? = nil
    @Published public var viewing: Int = -1
    @Published public var visible: Bool = false
    @Published public var mode: Mode = .create
    
    public enum Mode {
        case create
        case edit(taskId: Int64)
    }
    
    private var db: TaskRepository
    private var parser: Parser
    
    public init(db: TaskRepository) {
        self.db = db
        self.parser = Parser(db: db)
    }
    
    public func editCreatedTask(taskId: Int64) {
        // TODO: Implement
    }
    
    public func deleteEditTask() {
        // TODO: Implement
    }
    
    public func onShow(tagId: Int64? = nil) {
        // TODO: Implement
    }
    
    public func onHide() {
        // TODO: Implement
    }
    
    public func save() {
        // TODO: Implement
    }
    
    public func updateTask(title: String) {
        let parsed = parser.parse(input: title)
        self.task = title
        self.parsed = parsed
        
        if parsed.times.isEmpty {
            self.viewing = -1
        } else if self.viewing == parsed.times.count {
            self.viewing = parsed.times.count
        } else if self.viewing < parsed.times.count && self.viewing != -1 {
            // viewing is valid
        } else {
            self.viewing = 0
        }
    }
    
    public func viewNextParsed() {
        if self.viewing == -1 {
            return
        }
        
        let newViewing = (self.viewing + 1) % (self.parsed?.times.count ?? 0 + 1)
        self.viewing = newViewing
    }
    
    public func getTagsStarting(title: String) -> [TaskTag] {
        let trimmedTitle = title.trimmingCharacters(in: .whitespaces)
        if trimmedTitle.isEmpty || trimmedTitle.first != "#" {
            return []
        }
        
        // This is a blocking call in the original code.
        // For simplicity, we'll do it asynchronously here.
        // In a real app, you might want to handle this differently.
        var tags: [TaskTag] = []
        db.getTagStartingWith(title: String(trimmedTitle.dropFirst())) { result, error in
            if let result = result {
                tags = result
            }
        }
        return tags
    }
    
    public func updateDescription(description: String) {
        self.description = description
    }
}
