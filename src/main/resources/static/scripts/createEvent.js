document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('data-form');

    if (!form) {
        console.error('Форма не найдена! Проверьте ID формы в HTML');
        return;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        try {
            const formData = new FormData();
            formData.append('title', document.getElementById('event-name').value);
            formData.append('description', document.getElementById('event-description').value);
            formData.append('date', document.getElementById('event-date').value);
            formData.append('participantLimit', document.getElementById('event-participant-limit').value);
            formData.append('location', document.getElementById('event-location').value);
            formData.append('category', document.getElementById('event-category').value);

            const imageInput = document.getElementById('event-image');
            if (imageInput && imageInput.files.length > 0) {
                formData.append('image', imageInput.files[0]);
            }

            const response = await fetch('/api/events', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const modalElement = document.getElementById('exampleModal');
                const openerButton = document.querySelector('[data-bs-toggle="modal"][data-bs-target="#exampleModal"]');

                if (!response.ok) throw new Error(await response.text());

                closeModalProperly();

                alert('Мероприятие успешно создано!');
                form.reset();

                if (typeof loadEvents === 'function') loadEvents();
                } else if (response.status >= 300 && response.status < 400) {
                const location = response.headers.get('Location');
                alert(`Перенаправление на: ${location}`);
            } else if (response.status === 400) {
                alert('Неизвестная ошибка!')
            } else if (response.status === 401) {
                alert('Ошибка: Требуется авторизация');
            } else if (response.status === 403) {
                alert('Ошибка: Доступ запрещен');
            } else if (response.status === 404) {
                alert('Ошибка: Ресурс не найден');
            } else if (response.status === 409) {
                const error = await response.json();
                alert(`Ошибка: ${error.message || 'Конфликт данных'}`);
            } else if (response.status >= 400 && response.status < 500) {
                const error = await response.json().catch(() => ({ message: 'Клиентская ошибка' }));
                alert(`Ошибка: ${error.message || response.statusText}`);
            } else if (response.status >= 500) {
                alert('Ошибка сервера. Пожалуйста, попробуйте позже.');
            } else {
                alert(`Неожиданный статус ответа: ${response.status}`);
            }
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Сетевая ошибка: ' + error.message);
        }
    });
});

function closeModalProperly() {
    const modal = bootstrap.Modal.getInstance(document.getElementById('exampleModal'));
    if (!modal) return;

    modal._element.removeAttribute('aria-hidden');

    modal._element.style.display = 'none';
    modal._element.classList.remove('show');
    document.body.classList.remove('modal-open');

    const backdrops = document.querySelectorAll('.modal-backdrop');
    backdrops.forEach(backdrop => backdrop.remove());

    document.body.style.overflow = '';
    document.body.style.paddingRight = '';

    const openerBtn = document.querySelector('[data-bs-target="#exampleModal"]');
    if (openerBtn) openerBtn.focus();
}