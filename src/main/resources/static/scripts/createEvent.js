document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('data-form'); // или 'dataForm' - должен совпадать с HTML

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

            // Отправка на сервер
            const response = await fetch('/api/events', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                // Закрытие модального окна
                const modalElement = document.getElementById('exampleModal');
                if (modalElement) {
                    const modal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
                    modal.hide();
                }
                form.reset();
                alert('Мероприятие успешно создано!');
                if (typeof loadEvents === 'function') {
                    loadEvents();
                }
            } else if (response.status >= 300 && response.status < 400) {
                // Перенаправления (300-399)
                const location = response.headers.get('Location');
                alert(`Перенаправление на: ${location}`);
                // Можно автоматически перейти по адресу перенаправления
                // window.location.href = location;
            } else if (response.status === 400) {
                alert('Неизвестная ошибка!')
            } else if (response.status === 401) {
                // Не авторизован
                alert('Ошибка: Требуется авторизация');
                // Можно перенаправить на страницу входа
                //window.location.href = '/login';
            } else if (response.status === 403) {
                // Доступ запрещен
                alert('Ошибка: Доступ запрещен');
            } else if (response.status === 404) {
                // Не найдено
                alert('Ошибка: Ресурс не найден');
            } else if (response.status === 409) {
                // Конфликт
                const error = await response.json();
                alert(`Ошибка: ${error.message || 'Конфликт данных'}`);
            } else if (response.status >= 400 && response.status < 500) {
                // Другие клиентские ошибки (400-499)
                const error = await response.json().catch(() => ({ message: 'Клиентская ошибка' }));
                alert(`Ошибка: ${error.message || response.statusText}`);
            } else if (response.status >= 500) {
                // Серверные ошибки (500-599)
                alert('Ошибка сервера. Пожалуйста, попробуйте позже.');
            } else {
                // Другие статусы
                alert(`Неожиданный статус ответа: ${response.status}`);
            }
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Сетевая ошибка: ' + error.message);
        }
    });
});