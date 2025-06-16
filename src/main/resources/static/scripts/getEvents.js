const eventsContainer = document.getElementById('events-container');
const modalElement = document.getElementById('full-modal-event');
const modalBody = modalElement.querySelector('.modal-body');
const subscribeBtn = modalElement.querySelector('.btn-primary');

let currentEventId = null;

let searchTimeout = null;
const SEARCH_DELAY = 300;

// Основная функция загрузки и отображения событий
async function loadEvents(searchQuery = "") {
    try {
        const url = new URL('/api/events', window.location.origin);
        if(searchQuery.trim()) {
            url.searchParams.append('title', searchQuery);
        }

        const response = await fetch(url);

        if (!response.ok) {throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const events = await response.json();
        renderEvents(events);


    } catch (error) {
        handleError(error);
    }
}

async function loadEventDetails(eventId) {
    try {
        const response = await fetch('/api/events/' + eventId);

        if (!response.ok) {
            throw new Error('HTTP error! Status: ${response.status}');
        }

        const eventDetails = await response.json();
        populateModal(eventDetails);
        currentEventId = eventDetails.id;

        subscribeBtn.disabled = false;
        subscribeBtn.textContent = 'Принять участие';
        modalBody.querySelectorAll('.alert').forEach(a => a.remove());

        const bsModal = new bootstrap.Modal(modalElement);
        bsModal.show();

    } catch (error) {
        handleError(error);
    }
}


function populateModal(eventDetails) {
    console.log(eventDetails);
    const modalTitle = document.querySelector('#full-modal-event .modal-title');
    const modalBody = document.querySelector('#full-modal-event .modal-body');

    modalTitle.textContent = eventDetails.title;

    modalBody.innerHTML = '';

    const description = document.createElement('p');
    description.className = 'card-text';
    description.innerHTML = `<h5>Описание:</h5> <p>${eventDetails.description}</p>`;

    const dateContainer = document.createElement('p');
    dateContainer.className = 'mb-1';
    dateContainer.innerHTML = `<h5>Дата проведения</h5><p class="far fa-calendar-alt me-2"></p>${formatDate(eventDetails.date)}`;

    if (eventDetails.image_url) {
        const image = document.createElement('img');
        image.src = eventDetails.image_url;
        image.alt = eventDetails.title;
        image.className = 'img-fluid mb-3';
        modalBody.appendChild(image);
    }
    const location = document.createElement('p');
    location.className = 'card-text';
    location.innerHTML = `<h5>Адрес мероприятия:</h5> <p>${eventDetails.location}</p>`;

    const participantLimit = document.createElement('p');
    participantLimit.className = 'card-text';
    participantLimit.innerHTML = `<h5>Количество участников:</h5> <p>${eventDetails.participantLimit}</p>`;
    const category = document.createElement('p');
    category.className = 'card-text';
    if (eventDetails.category == "SPORT")
    {
        category.innerHTML = `<h5>Категория:</h5> <p>Спорт</p>`;
    } else if (eventDetails.category == "SCIENCE") {
        category.innerHTML = `<h5>Категория:</h5> <p>Наука</p>`;
    } else {
        category.innerHTML = `<h5>Категория:</h5> <p>Культура</p>`;
    }

    modalBody.append(description, dateContainer, participantLimit, category, location);
}

function renderEvents(events) {
    eventsContainer.innerHTML = events
        .map(event => {
            const card = createEventCard({
                ...event,
                id: BigInt(event.id).toString()
            });

            const linkWrapper = document.createElement('a');
            linkWrapper.href = '#';
            linkWrapper.dataset.bsToggle = 'modal';
            linkWrapper.dataset.bsTarget = '#full-modal-event';
            linkWrapper.dataset.eventId = event.id;

            linkWrapper.appendChild(card);
            return linkWrapper.outerHTML;
        })

        .join('');

    const modalLinks = eventsContainer.querySelectorAll('a[data-bs-toggle="modal"]');
    modalLinks.forEach(link => {
        link.addEventListener('click', async (e) => {
            e.preventDefault();
            const eventId = link.dataset.eventId;

            await loadEventDetails(eventId);
        });
    });

}

function createEventCard(event) {
    const card = document.createElement('div');

    card.className = 'event-card card mb-4 rounded-5';

    const cardBody = document.createElement('div');
    cardBody.className = 'card-body rounded-5 position-relative';

    cardBody.style.background = `
        linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.45)),
        ${event.image_url
        ? `url('${event.image_url}') no-repeat center center/cover`
        : '#f0f0f0'
    }
    `;
    cardBody.style.height = '50vh';

    const title = document.createElement('h3');
    title.className = 'card-title';
    title.textContent = event.title;

    const date = document.createElement('p');
    date.className = 'mb-1';

    const description = document.createElement('p');
    description.className = 'card-text';
    description.textContent = event.description;

    cardBody.append(title, date, description);
    card.appendChild(cardBody);

    return card;
}

function handleError(error) {
    console.error('Ошибка:', error);
    eventsContainer.innerHTML = `
        <div class="alert alert-danger">
            Ошибка загрузки: ${error.message}
        </div>
    `;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function handleSearchInput() {
    const searchTerm = document.getElementById('search-input').value.trim();
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => loadEvents(searchTerm), SEARCH_DELAY);
}

function handleFormSubmit(e) {
    e.preventDefault();
    clearTimeout(searchTimeout);
    const searchTerm = document.getElementById('search-input').value.trim();
    loadEvents(searchTerm);
}

// Обработчик клика кнопки "Принять участие"
subscribeBtn.addEventListener('click', async () => {
    if (!currentEventId) return;

    subscribeBtn.disabled = true;
    subscribeBtn.textContent = 'Отправка...';

    modalBody.querySelectorAll('.alert').forEach(a => a.remove());

    try {
        const response = await fetch(`/api/events/${currentEventId}/subscribe`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            throw new Error(`Ошибка: ${response.status}`);
        }

        const qrCodeUrl = await response.text();

        const qrResponse = await fetch(qrCodeUrl);
        if (!qrResponse.ok) throw new Error('Ошибка при скачивании QR-кода');

        const blob = await qrResponse.blob();
        const blobUrl = URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = blobUrl;
        link.download = `qrcode_event_${currentEventId}.png`;

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        URL.revokeObjectURL(blobUrl);

        subscribeBtn.textContent = 'Вы подписаны';
        subscribeBtn.disabled = true;

        modalBody.insertAdjacentHTML('beforeend', `
        <div class="alert alert-success mt-3">
            Вы успешно подписались! QR-код скачан автоматически.
        </div>
    `);

    } catch (error) {
        modalBody.insertAdjacentHTML('beforeend', `
            <div class="alert alert-danger mt-3">
                Не удалось подписаться
            </div>
        `);
        subscribeBtn.textContent = 'Принять участие';
        subscribeBtn.disabled = false;
    }
});

document.addEventListener('DOMContentLoaded', () => {
    loadEvents();

    const searchInput = document.getElementById('search-input');
    const searchForm = document.querySelector('.search-form');

    if (searchInput) searchInput.addEventListener('input', handleSearchInput);
    if (searchForm) searchForm.addEventListener('submit', handleFormSubmit);
});