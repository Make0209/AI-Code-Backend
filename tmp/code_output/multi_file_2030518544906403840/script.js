// Task data storage (using localStorage for persistence)
let tasks = JSON.parse(localStorage.getItem('tasks')) || [];

// DOM elements
const taskForm = document.getElementById('taskForm');
const taskList = document.getElementById('taskList');
const filterButtons = document.querySelectorAll('.filter-btn');

// Task class to represent a task
class Task {
    constructor(title, description, dueDate, priority) {
        this.id = Date.now(); // Simple unique ID based on timestamp
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }
}

// Function to add a new task
function addTask(event) {
    event.preventDefault();
    
    const title = document.getElementById('taskTitle').value.trim();
    const description = document.getElementById('taskDescription').value.trim();
    const dueDate = document.getElementById('taskDueDate').value;
    const priority = document.getElementById('taskPriority').value;
    
    if (!title || !dueDate) {
        alert('Please fill in the required fields: Title and Due Date.');
        return;
    }
    
    const newTask = new Task(title, description, dueDate, priority);
    tasks.push(newTask);
    saveTasks();
    renderTasks();
    taskForm.reset(); // Clear the form
}

// Function to save tasks to localStorage
function saveTasks() {
    localStorage.setItem('tasks', JSON.stringify(tasks));
}

// Function to render tasks based on filter
function renderTasks(filter = 'all') {
    taskList.innerHTML = ''; // Clear current list
    
    let filteredTasks = tasks;
    if (filter === 'pending') {
        filteredTasks = tasks.filter(task => !task.completed);
    } else if (filter === 'completed') {
        filteredTasks = tasks.filter(task => task.completed);
    }
    
    filteredTasks.forEach(task => {
        const taskItem = document.createElement('li');
        taskItem.className = `task-item ${task.priority} ${task.completed ? 'completed' : ''}`;
        taskItem.dataset.id = task.id;
        
        const formattedDate = new Date(task.dueDate).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
        
        taskItem.innerHTML = `
            <div class="task-content">
                <h3>${task.title}</h3>
                ${task.description ? `<p>${task.description}</p>` : ''}
                <p class="due-date">Due: ${formattedDate}</p>
                <p>Priority: <span class="priority">${task.priority}</span></p>
            </div>
            <div class="task-actions">
                <button class="complete-btn">${task.completed ? 'Undo' : 'Complete'}</button>
                <button class="delete-btn">Delete</button>
            </div>
        `;
        
        taskList.appendChild(taskItem);
    });
    
    // Add event listeners to the new buttons
    attachTaskEventListeners();
}

// Function to attach event listeners to task buttons
function attachTaskEventListeners() {
    document.querySelectorAll('.complete-btn').forEach(button => {
        button.addEventListener('click', function() {
            const taskId = parseInt(this.closest('.task-item').dataset.id);
            toggleTaskCompletion(taskId);
        });
    });
    
    document.querySelectorAll('.delete-btn').forEach(button => {
        button.addEventListener('click', function() {
            const taskId = parseInt(this.closest('.task-item').dataset.id);
            deleteTask(taskId);
        });
    });
}

// Function to toggle task completion status
function toggleTaskCompletion(taskId) {
    const taskIndex = tasks.findIndex(task => task.id === taskId);
    if (taskIndex !== -1) {
        tasks[taskIndex].completed = !tasks[taskIndex].completed;
        saveTasks();
        renderTasks(getCurrentFilter());
    }
}

// Function to delete a task
function deleteTask(taskId) {
    if (confirm('Are you sure you want to delete this task?')) {
        tasks = tasks.filter(task => task.id !== taskId);
        saveTasks();
        renderTasks(getCurrentFilter());
    }
}

// Function to get the current active filter
function getCurrentFilter() {
    const activeButton = document.querySelector('.filter-btn.active');
    return activeButton ? activeButton.dataset.filter : 'all';
}

// Event listener for form submission
taskForm.addEventListener('submit', addTask);

// Event listeners for filter buttons
filterButtons.forEach(button => {
    button.addEventListener('click', function() {
        // Remove active class from all buttons
        filterButtons.forEach(btn => btn.classList.remove('active'));
        // Add active class to clicked button
        this.classList.add('active');
        // Render tasks with the selected filter
        renderTasks(this.dataset.filter);
    });
});

// Initial render of tasks on page load
document.addEventListener('DOMContentLoaded', () => {
    renderTasks();
});