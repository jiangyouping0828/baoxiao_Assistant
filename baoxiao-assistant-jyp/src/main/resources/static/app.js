/* 报销助手（JYP）通用前端脚本 */

async function api(path, options = {}) {
    const opts = Object.assign({headers: {'Content-Type': 'application/json'}}, options);
    if (opts.body && typeof opts.body !== 'string') {
        opts.body = JSON.stringify(opts.body);
    }
    const resp = await fetch(path, opts);
    if (resp.status === 401) {
        window.location.href = '/login';
        throw new Error('未登录');
    }
    const text = await resp.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch (e) {
        data = text;
    }
    if (!resp.ok) {
        const msg = (data && data.error) ? data.error : ('请求失败（' + resp.status + '）');
        throw new Error(msg);
    }
    return data;
}

function esc(value) {
    if (value === null || value === undefined) {
        return '';
    }
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function showAlert(el, message, ok) {
    if (!el) {
        return;
    }
    el.textContent = message;
    el.className = 'alert show ' + (ok ? 'ok' : 'err');
    if (ok) {
        setTimeout(() => {
            el.className = 'alert';
        }, 3000);
    }
}

async function logout() {
    try {
        await api('/api/auth/logout', {method: 'POST'});
    } catch (e) {
        /* ignore */
    }
    window.location.href = '/login';
}

function money(value) {
    if (value === null || value === undefined) {
        return '-';
    }
    return '¥' + Number(value).toFixed(2);
}
