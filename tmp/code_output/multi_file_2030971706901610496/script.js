// 登录表单交互逻辑

// 等待DOM完全加载
document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const submitBtn = document.getElementById('submitBtn');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const btnText = document.querySelector('.btn-text');
    const successMessage = document.getElementById('successMessage');
    const usernameError = document.getElementById('usernameError');
    const passwordError = document.getElementById('passwordError');
    
    // 密码显示/隐藏切换
    let isPasswordVisible = false;
    
    togglePasswordBtn.addEventListener('click', function() {
        isPasswordVisible = !isPasswordVisible;
        
        if (isPasswordVisible) {
            passwordInput.type = 'text';
            togglePasswordBtn.textContent = '🙈';
            togglePasswordBtn.setAttribute('aria-label', '隐藏密码');
        } else {
            passwordInput.type = 'password';
            togglePasswordBtn.textContent = '👁️';
            togglePasswordBtn.setAttribute('aria-label', '显示密码');
        }
    });
    
    // 表单验证函数
    function validateForm() {
        let isValid = true;
        
        // 清除之前的错误信息
        usernameError.textContent = '';
        passwordError.textContent = '';
        
        // 验证用户名/邮箱
        const username = usernameInput.value.trim();
        if (!username) {
            usernameError.textContent = '请输入用户名或邮箱';
            isValid = false;
        } else if (username.length < 3) {
            usernameError.textContent = '用户名至少需要3个字符';
            isValid = false;
        }
        
        // 验证密码
        const password = passwordInput.value.trim();
        if (!password) {
            passwordError.textContent = '请输入密码';
            isValid = false;
        } else if (password.length < 6) {
            passwordError.textContent = '密码至少需要6个字符';
            isValid = false;
        }
        
        return isValid;
    }
    
    // 实时输入验证
    usernameInput.addEventListener('input', function() {
        const username = usernameInput.value.trim();
        if (username && username.length < 3) {
            usernameError.textContent = '用户名至少需要3个字符';
        } else {
            usernameError.textContent = '';
        }
    });
    
    passwordInput.addEventListener('input', function() {
        const password = passwordInput.value.trim();
        if (password && password.length < 6) {
            passwordError.textContent = '密码至少需要6个字符';
        } else {
            passwordError.textContent = '';
        }
    });
    
    // 模拟登录API调用
    function simulateLogin(username, password, rememberMe) {
        return new Promise((resolve, reject) => {
            // 模拟网络延迟
            setTimeout(() => {
                // 模拟成功登录（在实际应用中，这里会调用真实的API）
                // 为了演示目的，我们假设任何非空用户名和密码都有效
                if (username && password) {
                    resolve({
                        success: true,
                        message: '登录成功',
                        user: {
                            name: username,
                            token: 'mock-jwt-token-12345'
                        }
                    });
                } else {
                    reject({
                        success: false,
                        message: '用户名或密码错误'
                    });
                }
            }, 1500);
        });
    }
    
    // 表单提交处理
    loginForm.addEventListener('submit', async function(event) {
        event.preventDefault();
        
        // 验证表单
        if (!validateForm()) {
            return;
        }
        
        // 获取表单数据
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        const rememberMe = document.getElementById('remember').checked;
        
        // 显示加载状态
        submitBtn.disabled = true;
        loadingSpinner.style.display = 'block';
        btnText.textContent = '登录中...';
        
        try {
            // 模拟API调用
            const result = await simulateLogin(username, password, rememberMe);
            
            if (result.success) {
                // 登录成功
                console.log('登录成功:', result.user);
                
                // 隐藏表单，显示成功消息
                loginForm.style.display = 'none';
                successMessage.style.display = 'block';
                
                // 在实际应用中，这里会保存token并重定向到仪表板
                // 例如：localStorage.setItem('authToken', result.user.token);
                
                // 模拟重定向延迟
                setTimeout(() => {
                    alert(`欢迎回来，${result.user.name}! 在实际应用中，您将被重定向到仪表板。`);
                    // 在实际应用中：window.location.href = '/dashboard';
                }, 2000);
            }
        } catch (error) {
            // 登录失败
            console.error('登录失败:', error.message);
            
            // 显示错误信息
            if (error.message.includes('用户名或密码')) {
                passwordError.textContent = '用户名或密码错误';
            } else {
                passwordError.textContent = '登录失败，请稍后重试';
            }
            
            // 恢复按钮状态
            submitBtn.disabled = false;
            loadingSpinner.style.display = 'none';
            btnText.textContent = '登录';
        }
    });
    
    // 忘记密码链接点击事件
    document.querySelector('.forgot-password').addEventListener('click', function(event) {
        event.preventDefault();
        const email = prompt('请输入您的邮箱地址以重置密码:');
        if (email) {
            alert(`重置密码链接已发送到 ${email}。\n（此功能仅为演示）`);
        }
    });
    
    // 注册链接点击事件
    document.querySelector('.register-link').addEventListener('click', function(event) {
        event.preventDefault();
        alert('注册功能正在开发中...');
    });
    
    // 初始焦点设置
    usernameInput.focus();
    
    // 添加键盘快捷键支持
    document.addEventListener('keydown', function(event) {
        // Ctrl+Enter 提交表单
        if (event.ctrlKey && event.key === 'Enter') {
            if (!submitBtn.disabled) {
                loginForm.dispatchEvent(new Event('submit'));
            }
        }
        
        // Esc 键清除表单
        if (event.key === 'Escape') {
            if (confirm('确定要清除表单内容吗？')) {
                loginForm.reset();
                usernameError.textContent = '';
                passwordError.textContent = '';
            }
        }
    });
    
    // 控制台欢迎信息
    console.log('登录界面已加载完成。');
    console.log('提示: 可以使用 Ctrl+Enter 快速提交表单，Esc 键清除表单。');
});