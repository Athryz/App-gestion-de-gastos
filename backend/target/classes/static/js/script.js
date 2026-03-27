// --- VARIABLES GLOBALES ---
let calendar;

// --- FUNCIONES DE ALCANCE GLOBAL (Para que FullCalendar las vea) ---

function prepararModal(movimiento, fecha) {
    const modal = document.getElementById('modalMovimiento');
    const role = localStorage.getItem('userRole');
    
    document.getElementById('modalFechaText').innerText = "Date: " + fecha;
    modal.classList.remove('hidden');

    document.getElementById('desc').value = movimiento ? movimiento.description : "";
    document.getElementById('monto').value = movimiento ? movimiento.amount : "";
    document.getElementById('tipo').value = movimiento ? movimiento.category : "INCOME";

    const btnG = document.getElementById('btnGuardar');
    const btnB = document.getElementById('btnBorrar');

    if (movimiento) {
        const isBasic = role === 'BASIC';
        document.querySelectorAll('.modal-input, .modal-select').forEach(i => i.disabled = isBasic);

        if (isBasic) {
            btnG.classList.add('hidden');
            btnB.classList.add('hidden');
        } else {
            btnG.classList.remove('hidden');
            btnB.classList.remove('hidden');
            btnG.onclick = () => editarMovimiento(movimiento.id, fecha);
            btnB.onclick = () => eliminarMovimiento(movimiento.id);
        }
    } else {
        document.querySelectorAll('.modal-input, .modal-select').forEach(i => i.disabled = false);
        btnG.classList.remove('hidden');
        btnB.classList.add('hidden');
        btnG.onclick = () => guardarMovimiento(fecha);
    }
}

async function guardarMovimiento(fecha) {
    const userId = localStorage.getItem('userId');
    const mov = {
        description: document.getElementById('desc').value,
        amount: parseFloat(document.getElementById('monto').value),
        category: document.getElementById('tipo').value,
        date: fecha
    };
    try {
        const res = await fetch(`http://localhost:8080/api/movements/${userId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(mov)
        });
        if (res.ok) { 
            cerrarModal(); 
            location.reload(); // Recarga simple para refrescar datos
        }
    } catch (e) { console.error(e); }
}

async function editarMovimiento(id, fecha) {
    const userId = localStorage.getItem('userId');
    const movData = {
        description: document.getElementById('desc').value,
        amount: parseFloat(document.getElementById('monto').value),
        category: document.getElementById('tipo').value,
        date: fecha
    };
    try {
        const res = await fetch(`http://localhost:8080/api/movements/${id}?requesterId=${userId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(movData)
        });
        if (res.ok) { cerrarModal(); location.reload(); }
    } catch (e) { console.error("Error editando:", e); }
}

async function eliminarMovimiento(id) {
    if(!confirm("Are you sure?")) return;
    const userId = localStorage.getItem('userId');
    try {
        const res = await fetch(`http://localhost:8080/api/movements/${id}?requesterId=${userId}`, { method: 'DELETE' });
        if (res.ok) { cerrarModal(); location.reload(); }
    } catch (e) { console.error("Error eliminando:", e); }
}

window.cerrarModal = () => {
    document.getElementById('modalMovimiento').classList.add('hidden');
};

window.cerrarSesion = () => { 
    localStorage.clear(); 
    location.reload(); 
};

window.toggleNotificaciones = (e) => {
    e.stopPropagation();
    document.getElementById('notif-dropdown').classList.toggle('hidden');
};

// --- LÓGICA DE INICIO Y DOM ---

document.addEventListener('DOMContentLoaded', function () {
    const sectionAuth = document.getElementById('section-auth');
    const sectionApp = document.getElementById('section-app');

    if (localStorage.getItem('userId')) mostrarApp();

    function mostrarApp() {
        sectionAuth.classList.add('hidden');
        sectionApp.classList.remove('hidden');
        document.getElementById('user-display-name').innerText = localStorage.getItem('userName') || "Guest";
        
        const role = localStorage.getItem('userRole') || "BASIC";
        const badge = document.getElementById('role-badge');
        badge.innerText = role;
        badge.className = `role-badge badge-${role.toLowerCase()}`;

        inicializarCalendario();
        cargarNotificaciones();
        setInterval(cargarNotificaciones, 30000);
    }

    function inicializarCalendario() {
        const calendarEl = document.getElementById('calendar');
        if (!calendarEl) return;
        
        calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            height: '100%', 
            expandRows: true,
            handleWindowResize: true,
            locale: 'en',
            headerToolbar: { 
                left: 'prev,next today', 
                center: 'title', 
                right: 'dayGridMonth,dayGridWeek' 
            },
            dateClick: (info) => prepararModal(null, info.dateStr),
            eventClick: (info) => {
                const m = info.event.extendedProps;
                prepararModal({ id: info.event.id, ...m }, info.event.startStr);
            }
        });
        calendar.render();
        cargarMovimientos();
    }

    async function cargarNotificaciones() {
        const userId = localStorage.getItem('userId');
        if (!userId) return;
        try {
            const res = await fetch(`http://localhost:8080/api/notificaciones/user/${userId}`);
            const data = await res.json();
            const list = document.getElementById('notificaciones-list');
            const badge = document.getElementById('notif-count');
            
            const unread = data.filter(n => !n.read).length;
            badge.innerText = unread;
            unread > 0 ? badge.classList.remove('hidden') : badge.classList.add('hidden');

            list.innerHTML = data.length === 0 ? "<li style='text-align:center; padding:10px;'>No alerts</li>" : "";
            
            data.sort((a,b) => new Date(b.date) - new Date(a.date)).forEach(n => {
                const li = document.createElement('li');
                li.className = n.read ? '' : 'unread';
                li.innerHTML = `<strong>${new Date(n.date).toLocaleDateString()}</strong><br>${n.message}`;
                list.appendChild(li);
            });
        } catch (e) { console.warn("Notifications offline."); }
    }

    async function cargarMovimientos() {
        const userId = localStorage.getItem('userId');
        try {
            const res = await fetch(`http://localhost:8080/api/movements/user/${userId}`);
            const data = await res.json();
            
            calendar.removeAllEvents();
            let total = 0;

            data.forEach(m => {
                const color = m.category === "INCOME" ? "#ff8fa3" : "#fb6f92";
                calendar.addEvent({
                    id: m.id, 
                    title: `${m.description} ($${m.amount})`, 
                    start: m.date,
                    backgroundColor: color, 
                    borderColor: 'white',
                    extendedProps: { ...m }
                });
                total += (m.category === "INCOME" ? parseFloat(m.amount) : -parseFloat(m.amount));
            });

            const el = document.getElementById('saldo');
            el.innerText = `Balance: $${total.toFixed(2)}`;
            el.className = 'balance-display ' + (total >= 0 ? 'balance-positive' : 'balance-negative');
        } catch (e) { console.error(e); }
    }

    // --- AUTH LOGIC ---

    document.getElementById('auth-form').onsubmit = async (e) => {
        e.preventDefault();
        const isLogin = document.getElementById('name-group').classList.contains('hidden');
        const payload = {
            email: document.getElementById('auth-email').value,
            password: document.getElementById('auth-password').value,
            name: document.getElementById('auth-name').value
        };
        try {
            const res = await fetch(`http://localhost:8080/api/auth/${isLogin ? 'login' : 'register'}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                const user = await res.json();
                localStorage.setItem('userId', user.id);
                localStorage.setItem('userName', user.name);
                localStorage.setItem('userRole', user.role);
                mostrarApp();
            } else { alert("Acceso denegado."); }
        } catch (e) { alert("Servidor no disponible."); }
    };

    document.getElementById('auth-toggle').onclick = () => {
        const nameGroup = document.getElementById('name-group');
        const authTitle = document.getElementById('auth-title');
        const authSubmit = document.getElementById('auth-submit');
        const authToggle = document.getElementById('auth-toggle');

        nameGroup.classList.toggle('hidden');
        const isLoginMode = nameGroup.classList.contains('hidden');

        if (isLoginMode) {
            authTitle.innerText = "Bienvenido de nuevo";
            authSubmit.innerText = "Iniciar Sesión";
            authToggle.innerText = "Aun no tienes cuenta? Regístrate aquí";
        } else {
            authTitle.innerText = "Crear Cuenta";
            authSubmit.innerText = "Registrarse";
            authToggle.innerText = "Ya tienes una cuenta? inicia sesión aquí";
        }
    };

    // Cierra el dropdown si haces click fuera
    document.addEventListener('click', () => {
        const dropdown = document.getElementById('notif-dropdown');
        if (dropdown) dropdown.classList.add('hidden');
    });
});