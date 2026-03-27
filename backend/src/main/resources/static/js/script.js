document.addEventListener('DOMContentLoaded', function () {
    const sectionAuth = document.getElementById('section-auth');
    const sectionApp = document.getElementById('section-app');
    const modal = document.getElementById('modalMovimiento');
    let calendar;

    const userId = localStorage.getItem('userId');
    if (userId) mostrarApp();

    function mostrarApp() {
        sectionAuth.classList.add('hidden');
        sectionApp.classList.remove('hidden');
        document.getElementById('user-display-name').innerText = localStorage.getItem('userName') || "Usuario";
        
        const roleBadge = document.getElementById('role-badge');
        if (roleBadge) {
            const role = localStorage.getItem('userRole') || "USER";
            roleBadge.innerText = role;
            roleBadge.className = `badge-${role.toLowerCase()}`;
        }

        inicializarCalendario();
    }

    window.cerrarSesion = function() {
        localStorage.clear();
        location.reload();
    };

    window.cerrarModal = function() {
        modal.classList.add('hidden');
    };

    // Lógica Auth
    const authForm = document.getElementById('auth-form');
    document.getElementById('auth-toggle').onclick = () => {
        const nameGrp = document.getElementById('name-group');
        nameGrp.classList.toggle('hidden');
        document.getElementById('auth-title').innerText = nameGrp.classList.contains('hidden') ? "Iniciar Sesión" : "Crear Cuenta";
    };

    authForm.onsubmit = async (e) => {
        e.preventDefault();
        const isLogin = document.getElementById('name-group').classList.contains('hidden');
        const payload = {
            email: document.getElementById('auth-email').value,
            password: document.getElementById('auth-password').value,
            name: document.getElementById('auth-name').value
        };
        const url = isLogin ? '/api/auth/login' : '/api/auth/register';
        try {
            const res = await fetch(`http://localhost:8080${url}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                const user = await res.json();
                localStorage.setItem('userId', user.id);
                localStorage.setItem('userName', user.name);
                localStorage.setItem('userRole', user.role || "USER");
                mostrarApp();
            } else { alert("Error en credenciales"); }
        } catch (error) { alert("Error de conexión"); }
    };

    function inicializarCalendario() {
    const calendarEl = document.getElementById('calendar');
    if (!calendarEl) return;
    
    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'es',
        showNonCurrentDates: false,
        fixedMirrorParent: true,
        dateClick: function(info) {
            // MODO CREAR: Pasamos null como movimiento
            prepararModal(null, info.dateStr);
        },
        eventClick: function(info) {
            // MODO EDITAR: Extraemos datos del evento de FullCalendar
            const evento = info.event;
            
            // Lógica para limpiar el título: "Cena ($50.00)" -> Desc: "Cena", Monto: "50.00"
            const titulo = evento.title;
            const montoMatch = titulo.match(/\(\$(\d+\.?\d*)\)/);
            const monto = montoMatch ? montoMatch[1] : 0;
            
            // Quitamos el prefijo de usuario si existe [Nombre]
            let desc = titulo.split('] ').pop(); 
            desc = desc.split(' ($')[0]; // Quitamos el ($monto)

            const esIngreso = evento.backgroundColor === "#28a745" || evento.backgroundColor === "rgb(40, 167, 69)";

            prepararModal({
                id: evento.id,
                description: desc,
                amount: monto,
                category: esIngreso ? "INCOME" : "EXPENSE"
            }, evento.startStr);
        }
    });
    calendar.render();
    cargarMovimientos();
}

function prepararModal(movimiento, fecha) {
    const titulo = document.getElementById('modalTitulo');
    const btnGuardar = document.getElementById('btnGuardar');
    const btnBorrar = document.getElementById('btnBorrar');
    const inputDesc = document.getElementById('desc');
    const inputMonto = document.getElementById('monto');
    const selectTipo = document.getElementById('tipo');

    document.getElementById('modalFecha').innerText = "Día: " + fecha;
    modal.classList.remove('hidden');

    if (movimiento) {
        // --- ESTADO: MODIFICAR/BORRAR ---
        titulo.innerText = "Editar Movimiento";
        inputDesc.value = movimiento.description;
        inputMonto.value = movimiento.amount;
        selectTipo.value = movimiento.category;
        
        btnGuardar.innerText = "Actualizar Cambios";
        btnGuardar.className = "btn-primary"; // O el color que prefieras para editar
        btnGuardar.onclick = () => editarMovimiento(movimiento.id, fecha);
        
        // Mostrar botón borrar solo si es el dueño o Admin/SuperAdmin
        btnBorrar.classList.remove('hidden');
        btnBorrar.onclick = () => {
            if(confirm("¿Estás seguro de eliminar este registro?")) {
                eliminarMovimiento(movimiento.id);
                cerrarModal();
            }
        };
    } else {
        // --- ESTADO: AGREGAR NUEVO ---
        titulo.innerText = "Nuevo Movimiento";
        inputDesc.value = "";
        inputMonto.value = "";
        selectTipo.value = "EXPENSE";
        
        btnGuardar.innerText = "Guardar";
        btnGuardar.className = "btn-success";
        btnGuardar.onclick = () => guardarMovimiento(fecha);
        
        btnBorrar.classList.add('hidden');
    }
}

    // --- NUEVA FUNCIÓN: Configura el modal según la acción ---
    function prepararModal(movimiento, fecha) {
        const titulo = document.getElementById('modalTitulo');
        const btnGuardar = document.getElementById('btnGuardar');
        const btnBorrar = document.getElementById('btnBorrar');
        const inputDesc = document.getElementById('desc');
        const inputMonto = document.getElementById('monto');
        const selectTipo = document.getElementById('tipo');

        document.getElementById('modalFecha').innerText = "Fecha: " + fecha;
        modal.classList.remove('hidden');

        if (movimiento) {
            // Configuración para EDITAR
            titulo.innerText = "Editar Movimiento";
            inputDesc.value = movimiento.description;
            inputMonto.value = movimiento.amount;
            selectTipo.value = movimiento.category;
            
            btnGuardar.innerText = "Actualizar Cambios";
            btnGuardar.onclick = () => editarMovimiento(movimiento.id, fecha);
            
            if (btnBorrar) {
                btnBorrar.classList.remove('hidden');
                btnBorrar.onclick = () => {
                    if(confirm("¿Seguro que quieres borrar este registro?")) {
                        eliminarMovimiento(movimiento.id);
                        cerrarModal();
                    }
                };
            }
        } else {
            // Configuración para CREAR
            titulo.innerText = "Agregar Movimiento";
            inputDesc.value = "";
            inputMonto.value = "";
            btnGuardar.innerText = "Guardar";
            btnGuardar.onclick = () => guardarMovimiento(fecha);
            if (btnBorrar) btnBorrar.classList.add('hidden');
        }
    }

    async function editarMovimiento(id, fecha) {
        const movData = {
            description: document.getElementById('desc').value,
            amount: parseFloat(document.getElementById('monto').value),
            category: document.getElementById('tipo').value,
            date: fecha
        };

        const res = await fetch(`http://localhost:8080/api/movements/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(movData)
        });

        if (res.ok) {
            cerrarModal();
            cargarMovimientos();
        } else {
            alert("No tienes permisos para editar este movimiento.");
        }
    }

    async function eliminarMovimiento(id) {
        const currentUserId = localStorage.getItem('userId');
        const res = await fetch(`http://localhost:8080/api/movements/${id}?requesterId=${currentUserId}`, {
            method: 'DELETE'
        });
        if (res.ok) {
            cargarMovimientos();
        } else {
            alert("No tienes permisos para borrar este movimiento.");
        }
    }

    async function guardarMovimiento(fecha) {
        const mov = {
            description: document.getElementById('desc').value,
            amount: parseFloat(document.getElementById('monto').value),
            category: document.getElementById('tipo').value,
            date: fecha,
            user: { id: parseInt(localStorage.getItem('userId')) }
        };
        const res = await fetch('http://localhost:8080/api/movements', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(mov)
        });
        if (res.ok) { 
            cerrarModal(); 
            cargarMovimientos(); 
            document.getElementById('desc').value = "";
            document.getElementById('monto').value = "";
        }
    }

    function cargarMovimientos() {
        const currentUserId = localStorage.getItem('userId');
        const userRole = localStorage.getItem('userRole') || "USER";

        fetch(`http://localhost:8080/api/movements/user/${currentUserId}`)
            .then(res => res.json())
            .then(data => {
                calendar.removeAllEvents();
                let totalSaldo = 0;
                data.forEach(m => {
                    let prefix = "";
                    if ((userRole === 'ADMIN' || userRole === 'SUPERADMIN') && m.user && m.user.id != currentUserId) {
                        prefix = `[${m.user.name}] `;
                    }

                    calendar.addEvent({
                        id: m.id,
                        title: `${prefix}${m.description} ($${m.amount})`,
                        start: m.date,
                        backgroundColor: m.category === "INCOME" ? "#28a745" : "#dc3545",
                        borderColor: m.category === "INCOME" ? "#28a745" : "#dc3545",
                        allDay: true
                    });
                    totalSaldo += (m.category === "INCOME" ? m.amount : -m.amount);
                });
                actualizarInterfazSaldo(totalSaldo);
            })
            .catch(err => console.error("Error cargando movimientos:", err));
    }

    function actualizarInterfazSaldo(total) {
        const el = document.getElementById('saldo');
        if (!el) return;

        el.innerText = `Balance Total: $${total.toFixed(2)}`;
        el.className = 'balance-display';

        if (total > 0) {
            el.classList.add('balance-positive');
        } else if (total < 0) {
            el.classList.add('balance-negative');
        } else {
            el.classList.add('balance-zero');
        }
    }
});