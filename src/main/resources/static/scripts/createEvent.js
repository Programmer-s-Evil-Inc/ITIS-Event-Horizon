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
            formData.append('name', document.getElementById('event-name').value);
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

                // Сброс формы
                form.reset();

                alert('Мероприятие успешно создано!');

                // Опционально: обновление списка событий
                if (typeof loadEvents === 'function') {
                    loadEvents();
                }
            } else {
                const error = await response.json().catch(() => ({ message: 'Неизвестная ошибка' }));
                alert(`Ошибка: ${error.message || response.status}`);
            }
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Сетевая ошибка: ' + error.message);
        }
    });
});