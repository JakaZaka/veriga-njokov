import { useEffect, useState } from 'react';
import '../WeatherTab.css';

function WeatherTab() {
  const [weather, setWeather] = useState(null);
  const [recommendations, setRecommendations] = useState(null);
  const [loading, setLoading] = useState(true);

  const defaultWeather = {
    location: "Ljubljana",
    temperature: 20,
    isRaining: false,
    isSnowing: false,
    fetchedAt: new Date().toISOString()
  };

  function getWeatherIcon(w) {
    if (w.isSnowing) return "❄️";
    if (w.isRaining) return "🌧️";
    if (w.temperature < 5) return "🥶";
    if (w.temperature < 15) return "🌥️";
    if (w.temperature < 25) return "🌤️";
    return "☀️";
  }

  useEffect(() => {
    async function fetchWeatherAndAdvice() {
      setLoading(true);
      let weatherData = null;
      try {
        const weatherRes = await fetch('/api/weather/current?location=Ljubljana');
        if (weatherRes.ok) {
          weatherData = await weatherRes.json();
        } else {
          weatherData = defaultWeather;
        }
      } catch {
        weatherData = defaultWeather;
      }
      setWeather(weatherData);

      // Fetch clothing recommendations (requires auth)
      let recData = null;
      try {
        const token = localStorage.getItem('token');
        const recRes = await fetch('/api/weather/recommendations?location=Ljubljana', {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
          credentials: 'include'
        });
        if (recRes.ok) {
          const recJson = await recRes.json();
          recData = recJson.recommendations;
        }
      } catch {
        recData = null;
      }
      if (!recData || !recData.weatherMessage) {
        recData = {
          weatherMessage: "Pleasant temperature. Light layers recommended.",
          regularItems: [],
          waterproofItems: []
        };
      }
      setRecommendations(recData);
      setLoading(false);
    }
    fetchWeatherAndAdvice();
  }, []);

  if (loading) return <div className="weather-tab weather-loading">Loading weather...</div>;
  if (!weather) return <div className="weather-tab weather-error">No weather data available.</div>;

  const date = new Date(weather.fetchedAt);
  const today = date.toLocaleDateString();

  return (
    <div className="weather-tab weather-card">
      <div className="weather-header">
        <span className="weather-icon">{getWeatherIcon(weather)}</span>
        <div>
          <div className="weather-title">Today's Weather</div>
          <div className="weather-date">{today}</div>
        </div>
      </div>
      <div className="weather-details">
        <div>
          <span className="weather-label">Location:</span> {weather.location}
        </div>
        <div>
          <span className="weather-label">Temperature:</span> <span className="weather-temp">{weather.temperature}°C</span>
        </div>
        <div>
          <span className="weather-label">Raining:</span> {weather.isRaining ? 'Yes' : 'No'}
        </div>
        <div>
          <span className="weather-label">Snowing:</span> {weather.isSnowing ? 'Yes' : 'No'}
        </div>
        <div className="weather-fetchedat">
          <span className="weather-label">Fetched at:</span> {date.toLocaleTimeString()}
        </div>
      </div>
      <hr />
      <div className="weather-recommend">
        <h3>How to dress today:</h3>
        <div className="weather-message">{recommendations?.weatherMessage || "No advice available."}</div>
        {recommendations?.regularItems && recommendations.regularItems.length > 0 && (
          <>
            <h4>Recommended items:</h4>
            <ul className="weather-items-list">
              {recommendations.regularItems.map(item => (
                <li key={item._id}>{item.name} <span className="weather-item-cat">({item.category})</span></li>
              ))}
            </ul>
          </>
        )}
        {recommendations?.waterproofItems && recommendations.waterproofItems.length > 0 && (
          <>
            <h4>Waterproof items:</h4>
            <ul className="weather-items-list">
              {recommendations.waterproofItems.map(item => (
                <li key={item._id}>{item.name} <span className="weather-item-cat">({item.category})</span></li>
              ))}
            </ul>
          </>
        )}
      </div>
    </div>
  );
}

export default WeatherTab;