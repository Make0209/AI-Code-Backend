// 任务记录网站 - JavaScript

// 全局变量
let tasks = [];
let currentFilter = 'all';
let taskToEdit = null;

// DOM元素
const taskInput = document.getElementById('taskInput');
const addTaskBtn = document.getElementById('addTaskBtn');
const tasksList = document.getElementById('tasksList');
const totalTasksElement = document.getElementById('totalTasks');
const completedTasksElement = document.getElementById('completedTasks');
const pendingTasksElement = document.getElementById('pendingTasks');
const filterButtons = document.querySelectorAll('.filter-btn');
const editModal = document.getElementById('editModal');
const editTaskInput = document.getElementById('editTaskInput');
const saveEditBtn = document.getElementById('saveEditBtn');
const cancelEditBtn = document.getElementById('cancelEditBtn');
const closeModalBtn = document.querySelector('.close-modal');
const currentYearElement = document.getElementById('currentYear');

// 初始化应用
function initApp() {
    // 设置当前年份
    currentYearElement.textContent = new Date().getFullYear();
    
    // 从本地存储加载任务
    loadTasksFromStorage();
    
    // 渲染任务列表
    renderTasks();
    
    // 更新任务统计
    updateTaskStats();
    
    // 设置事件监听器
    setupEventListeners();
}

// 从本地存储加载任务
function loadTasksFromStorage() {
    const storedTasks = localStorage.getItem('tasks');
    if (storedTasks) {
        tasks = JSON.parse(storedTasks);
    }
}

// 保存任务到本地存储
function saveTasksToStorage() {
    localStorage.setItem('tasks', JSON.stringify(tasks));
}

// 设置事件监听器
function setupEventListeners() {
    // 添加任务按钮点击事件
    addTaskBtn.addEventListener('click', addTask);
    
    // 任务输入框回车键事件
    taskInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            addTask();
        }
    });
    
    // 筛选按钮点击事件
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            // 移除所有按钮的active类
            filterButtons.forEach(btn => btn.classList.remove('active'));
            // 为当前按钮添加active类
            this.classList.add('active');
            // 更新当前筛选器
            currentFilter = this.getAttribute('data-filter');
            // 重新渲染任务列表
            renderTasks();
        });
    });
    
    // 编辑任务相关事件
    saveEditBtn.addEventListener('click', saveEditedTask);
    cancelEditBtn.addEventListener('click', closeEditModal);
    closeModalBtn.addEventListener('click', closeEditModal);
    
    // 点击模态框外部关闭模态框
    editModal.addEventListener('click', function(e) {
        if (e.target === editModal) {
            closeEditModal();
        }
    });
}

// 添加新任务
function addTask() {
    const taskText = taskInput.value.trim();
    
    if (taskText === '') {
        alert('请输入任务内容！');
        taskInput.focus();
        return;
    }
    
    // 创建新任务对象
    const newTask = {
        id: Date.now(), // 使用时间戳作为唯一ID
        text: taskText,
        completed: false,
        createdAt: new Date().toISOString()
    };
    
    // 添加到任务数组
    tasks.unshift(newTask);
    
    // 保存到本地存储
    saveTasksToStorage();
    
    // 清空输入框
    taskInput.value = '';
    
    // 重新渲染任务列表
    renderTasks();
    
    // 更新任务统计
    updateTaskStats();
    
    // 显示添加成功提示
    showNotification('任务添加成功！');
}

// 渲染任务列表
function renderTasks() {
    // 清空当前任务列表
    tasksList.innerHTML = '';
    
    // 根据筛选器过滤任务
    let filteredTasks = tasks;
    
    if (currentFilter === 'pending') {
        filteredTasks = tasks.filter(task => !task.completed);
    } else if (currentFilter === 'completed') {
        filteredTasks = tasks.filter(task => task.completed);
    }
    
    // 如果没有任务，显示空状态
    if (filteredTasks.length === 0) {
        const emptyMessage = document.createElement('li');
        emptyMessage.className = 'empty-message';
        emptyMessage.innerHTML = `
            <i class="fas fa-clipboard-list"></i>
            <p>${getEmptyMessage()}</p>
        `;
        tasksList.appendChild(emptyMessage);
        return;
    }
    
    // 渲染每个任务
    filteredTasks.forEach(task => {
        const taskItem = createTaskElement(task);
        tasksList.appendChild(taskItem);
    });
}

// 根据当前筛选器获取空状态消息
function getEmptyMessage() {
    switch(currentFilter) {
        case 'pending':
            return '暂无待完成任务，太棒了！';
        case 'completed':
            return '暂无已完成任务，加油！';
        default:
            return '暂无任务，请添加您的第一个任务！';
    }
}

// 创建任务元素
function createTaskElement(task) {
    const li = document.createElement('li');
    li.className = `task-item ${task.completed ? 'completed' : ''}`;
    li.setAttribute('data-id', task.id);
    
    li.innerHTML = `
        <div class="task-content">
            <input type="checkbox" class="task-checkbox" ${task.completed ? 'checked' : ''}>
            <span class="task-text ${task.completed ? 'completed' : ''}">${escapeHtml(task.text)}</span>
        </div>
        <div class="task-actions">
            <button class="action-btn edit-btn" title="编辑任务">
                <i class="fas fa-edit"></i>
            </button>
            <button class="action-btn delete-btn" title="删除任务">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;
    
    // 添加事件监听器
    const checkbox = li.querySelector('.task-checkbox');
    const editBtn = li.querySelector('.edit-btn');
    const deleteBtn = li.querySelector('.delete-btn');
    
    // 复选框点击事件 - 切换任务完成状态
    checkbox.addEventListener('change', function() {
        toggleTaskCompletion(task.id);
    });
    
    // 编辑按钮点击事件
    editBtn.addEventListener('click', function() {
        openEditModal(task);
    });
    
    // 删除按钮点击事件
    deleteBtn.addEventListener('click', function() {
        deleteTask(task.id);
    });
    
    return li;
}

// 切换任务完成状态
function toggleTaskCompletion(taskId) {
    // 找到任务索引
    const taskIndex = tasks.findIndex(task => task.id === taskId);
    
    if (taskIndex !== -1) {
        // 切换完成状态
        tasks[taskIndex].completed = !tasks[taskIndex].completed;
        
        // 保存到本地存储
        saveTasksToStorage();
        
        // 重新渲染任务列表
        renderTasks();
        
        // 更新任务统计
        updateTaskStats();
        
        // 显示通知
        const status = tasks[taskIndex].completed ? '已完成' : '待完成';
        showNotification(`任务标记为${status}`);
    }
}

// 打开编辑模态框
function openEditModal(task) {
    taskToEdit = task;
    editTaskInput.value = task.text;
    editModal.style.display = 'flex';
    editTaskInput.focus();
}

// 关闭编辑模态框
function closeEditModal() {
    editModal.style.display = 'none';
    taskToEdit = null;
    editTaskInput.value = '';
}

// 保存编辑后的任务
function saveEditedTask() {
    const newText = editTaskInput.value.trim();
    
    if (newText === '') {
        alert('任务内容不能为空！');
        editTaskInput.focus();
        return;
    }
    
    if (taskToEdit) {
        // 更新任务文本
        taskToEdit.text = newText;
        
        // 保存到本地存储
        saveTasksToStorage();
        
        // 重新渲染任务列表
        renderTasks();
        
        // 关闭模态框
        closeEditModal();
        
        // 显示通知
        showNotification('任务更新成功！');
    }
}

// 删除任务
function deleteTask(taskId) {
    if (confirm('确定要删除这个任务吗？')) {
        // 过滤掉要删除的任务
        tasks = tasks.filter(task => task.id !== taskId);
        
        // 保存到本地存储
        saveTasksToStorage();
        
        // 重新渲染任务列表
        renderTasks();
        
        // 更新任务统计
        updateTaskStats();
        
        // 显示通知
        showNotification('任务已删除');
    }
}

// 更新任务统计
function updateTaskStats() {
    const total = tasks.length;
    const completed = tasks.filter(task => task.completed).length;
    const pending = total - completed;
    
    totalTasksElement.textContent = `总任务: ${total}`;
    completedTasksElement.textContent = `已完成: ${completed}`;
    pendingTasksElement.textContent = `待完成: ${pending}`;
}

// 显示通知
function showNotification(message) {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background-color: #2575fc;
        color: white;
        padding: 15px 25px;
        border-radius: 8px;
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        z-index: 1001;
        font-weight: 600;
        animation: slideIn 0.3s ease-out;
    `;
    
    // 添加到页面
    document.body.appendChild(notification);
    
    // 3秒后移除通知
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
    
    // 添加动画样式
    if (!document.querySelector('#notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOut {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }
}

// 转义HTML特殊字符，防止XSS攻击
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 页面加载完成后初始化应用
document.addEventListener('DOMContentLoaded', initApp);