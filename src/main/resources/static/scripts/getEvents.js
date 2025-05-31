// Получаем элементы DOM
const eventsContainer = document.getElementById('events-container');
const eventTemplate = document.getElementById('event-template');

// Функция загрузки и отображения событий (асинхронно, шоб страница не висла)
async function loadEvents() {
    try {
        // У fetch по дефолту GET, так что явно не указываем
        const response = await fetch('http://localhost:8080/api/events');

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

// Рендеринг одного события (вряд ли корректно работает, но логика та же)
function createEventCard(event) {
    const imgUrl = event.image_url;
    const eventTitle = event.title;
    const eventDate = event.date;
    const eventId = event.id;
    const eventDescription = event.description;
    // Создаём контейнер карточки
    const card = document.createElement('div');
    card.className = 'event-card card mb-4 rounded-5';

    // Создаём ссылку-обёртку
    const link = document.createElement('a');

    // Основной контейнер контента
    const cardBody = document.createElement('div');
    cardBody.className = 'card-body rounded-5';
    cardBody.style.background = `
        linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.45)),
        url('${encodeURI(imgUrl)}') no-repeat center center / cover
    `;
    cardBody.style.height = '50vh';

    // Заголовок мероприятия
    const title = document.createElement('h3');
    title.className = 'card-title';
    title.textContent = eventTitle;

    // Блок с датой
    const date = document.createElement('p');
    date.innerHTML = `<i class="far fa-calendar-alt me-2"></i>${formatDate(eventDate)}`;

    // Описание мероприятия
    const description = document.createElement('p');
    description.className = 'card-text';
    description.textContent = eventDescription;

    const id = document.createElement('p');
    id.className = 'card-id';
    id.textContent = eventId;
    id.style.visibility = 'hidden';

    // Собираем структуру
    cardBody.append(title, date, description, id);
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

document.addEventListener('DOMContentLoaded', loadEvents);
