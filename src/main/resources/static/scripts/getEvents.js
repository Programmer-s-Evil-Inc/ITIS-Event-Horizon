// Получаем элементы DOM
const eventsContainer = document.getElementById('events-container');

let searchTimeout = null;
const SEARCH_DELAY = 300;

// Функция загрузки и отображения событий (асинхронно, шоб страница не висла)
async function loadEvents(searchQuery = "") {
    try {
        const url = new URL('/api/events', window.location.origin);
        if(searchQuery.trim()) {
            url.searchParams.append('title', searchQuery);
        }

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        // Получили ответ от бэка
        const events = await response.json();

        // Отрисовываем
        renderEvents(events);
    } catch (error) {
        handleError(error);
    }
}

// Рендеринг всех событий
function renderEvents(events) {
    eventsContainer.innerHTML = events
        .map(event => {
            const card = createEventCard({
                ...event,
                id: BigInt(event.id).toString()
            });
            return card.outerHTML;
        })
        .join('');
}

// Рендеринг одного события
function createEventCard(event) {
    const card = document.createElement('div');
    card.className = 'event-card card mb-4 rounded-5';

    const link = document.createElement('a');

    const cardBody = document.createElement('div');
    cardBody.className = 'card-body rounded-5 position-relative';

    // Фоновое изображение через imageUuid
    cardBody.style.background = `
        linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.45)),
        ${event.image_url
        ? `url('${event.image_url}') no-repeat center center/cover`
        : '#f0f0f0'
    }
    `;
    cardBody.style.height = '50vh';

    // Заголовок
    const title = document.createElement('h3');
    title.className = 'card-title';
    title.textContent = event.title;

    // Дата
    const date = document.createElement('p');
    date.className = 'mb-1';
    date.innerHTML = `<i class="far fa-calendar-alt me-2"></i>${formatDate(event.date)}`;

    // Описание
    const description = document.createElement('p');
    description.className = 'card-text';
    description.textContent = event.description;

    // Сборка элементов
    cardBody.append(
        title,
        date,
        description,
    );

    link.appendChild(cardBody);
    card.appendChild(link);

    return card;
}

function handleError(error) {
    console.error('Ошибка:', error);
    document.getElementById('events-container').innerHTML = `
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

// Функция для обработки поискового ввода
function handleSearchInput() {
    const searchTerm = document.getElementById('search-input').value.trim();

    // Очищаем предыдущий таймер
    clearTimeout(searchTimeout);

    // Устанавливаем новый таймер
    searchTimeout = setTimeout(() => {
        loadEvents(searchTerm);
    }, SEARCH_DELAY);
}

// Обработчик формы для поддержки отправки через Enter
function handleFormSubmit(e) {
    e.preventDefault();
    clearTimeout(searchTimeout);
    const searchTerm = document.getElementById('search-input').value.trim();
    loadEvents(searchTerm);
}

// Инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', () => {
    // Первоначальная загрузка событий
    loadEvents();

    const searchInput = document.getElementById('search-input');
    const searchForm = document.querySelector('.search-form');

    // Добавляем обработчики поисковой формы
    if (searchInput) {
        searchInput.addEventListener('input', handleSearchInput);
    }
    if (searchForm) {
        searchForm.addEventListener('submit', handleFormSubmit);
    }
});
