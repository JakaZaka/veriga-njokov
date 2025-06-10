import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../ClothingGrid.css';

const seasonOptions = [
  { key: "spring", icon: "🌸", label: "Spring" },
  { key: "summer", icon: "☀️", label: "Summer" },
  { key: "fall", icon: "🍂", label: "Fall" },
  { key: "winter", icon: "❄️", label: "Winter" },
  { key: "all", icon: "🌀", label: "All" }
];
const occasionOptions = [
  { key: "casual", label: "Casual" },
  { key: "formal", label: "Formal" },
  { key: "sport", label: "Sport" },
  { key: "party", label: "Party" },
  { key: "work", label: "Work" },
  { key: "other", label: "Other" }
];

function OutfitList() {
  const [outfits, setOutfits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [seasonFilters, setSeasonFilters] = useState([]);
  const [occasionFilters, setOccasionFilters] = useState([]);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [sidebarOutfit, setSidebarOutfit] = useState(null);
  const [sidebarItem, setSidebarItem] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchOutfits() {
      const token = localStorage.getItem('token');
      const res = await fetch('/api/outfits', {
        credentials: 'include',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      const data = await res.json();
      setOutfits(data);
      setLoading(false);
    }
    fetchOutfits();
  }, []);

  // Filtering logic
  const filteredOutfits = outfits.filter(outfit => {
    const matchesSeason =
      seasonFilters.length === 0 ||
      (outfit.season && (
        Array.isArray(outfit.season)
          ? outfit.season.some(s => seasonFilters.includes(s))
          : seasonFilters.includes(outfit.season)
      ));
    const matchesOccasion =
      occasionFilters.length === 0 ||
      (outfit.occasion && occasionFilters.includes(outfit.occasion));
    return matchesSeason && matchesOccasion;
  });

  // Handlers
  const toggleSeason = (season) => {
    setSeasonFilters(prev =>
      prev.includes(season)
        ? prev.filter(s => s !== season)
        : [...prev, season]
    );
  };
  const clearSeasonFilters = () => setSeasonFilters([]);
  const toggleOccasion = (occasion) => {
    setOccasionFilters(prev =>
      prev.includes(occasion)
        ? prev.filter(o => o !== occasion)
        : [...prev, occasion]
    );
  };
  const clearOccasionFilters = () => setOccasionFilters([]);

  // Sidebar handlers
  const openSidebar = (outfit) => {
    setSidebarOutfit(outfit);
    setSidebarOpen(true);
    setSidebarItem(null);
  };
  const closeSidebar = () => {
    setSidebarOpen(false);
    setSidebarOutfit(null);
    setSidebarItem(null);
  };

  // Open clothing item info in sidebar
  const handleItemClick = async (itemId) => {
    const token = localStorage.getItem('token');
    const res = await fetch(`/api/clothing/${itemId}`, {
      credentials: 'include',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const data = await res.json();
    setSidebarItem(data);
  };

  return (
    <div style={{ position: 'relative' }}>
      {/* Heading */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '18px',
        margin: '32px 0 18px 0'
      }}>
        <span style={{ fontSize: 44, color: "#1976d2" }}></span>
        <h2 style={{
          margin: 0,
          fontSize: '2.5rem',
          fontWeight: 800,
          color: '#1976d2',
          letterSpacing: '2px',
          fontFamily: "'Montserrat', 'Segoe UI', Arial, sans-serif",
          textShadow: '0 2px 8px #e3eafc'
        }}>
          Outfits
        </h2>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '32px', marginBottom: '1.5rem', flexWrap: 'wrap', justifyContent: 'center' }}>
        {/* Season Filter */}
        <div style={{ display: 'flex', gap: '14px', alignItems: 'center' }}>
          <span style={{ fontWeight: 600, color: '#1976d2' }}>Season:</span>
          <button
            onClick={clearSeasonFilters}
            style={{
              width: 36,
              height: 36,
              borderRadius: '6px',
              border: seasonFilters.length === 0 ? '2.5px solid #1976d2' : '1.5px solid #ccc',
              background: '#e3eafc',
              color: 'transparent',
              cursor: 'pointer',
              boxShadow: seasonFilters.length === 0 ? '0 2px 8px #e3eafc' : 'none',
              outline: seasonFilters.length === 0 ? '2px solid #1976d2' : 'none',
              marginBottom: 2,
              transition: 'border 0.15s'
            }}
            aria-pressed={seasonFilters.length === 0}
            title="All Seasons"
          >
            &nbsp;
          </button>
          {seasonOptions.map(season => (
            <button
              key={season.key}
              onClick={() => toggleSeason(season.key)}
              style={{
                width: 36,
                height: 36,
                borderRadius: '6px',
                border: seasonFilters.includes(season.key) ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                background: seasonFilters.includes(season.key) ? '#e3eafc' : '#fff',
                color: '#222',
                cursor: 'pointer',
                boxShadow: seasonFilters.includes(season.key) ? '0 2px 8px #e3eafc' : 'none',
                outline: seasonFilters.includes(season.key) ? '2px solid #1976d2' : 'none',
                marginBottom: 2,
                fontSize: '1.5em',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                transition: 'border 0.15s'
              }}
              aria-pressed={seasonFilters.includes(season.key)}
              title={season.label}
            >
              <span style={{ lineHeight: 1 }}>{season.icon}</span>
            </button>
          ))}
        </div>
        {/* Occasion Filter */}
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <span style={{ fontWeight: 600, color: '#1976d2' }}>Occasion:</span>
          <button
            onClick={clearOccasionFilters}
            style={{
              padding: '6px 10px',
              borderRadius: '6px',
              border: occasionFilters.length === 0 ? '2.5px solid #1976d2' : '1.5px solid #ccc',
              background: '#e3eafc',
              color: '#1976d2',
              fontWeight: 600,
              cursor: 'pointer',
              boxShadow: occasionFilters.length === 0 ? '0 2px 8px #e3eafc' : 'none',
              outline: occasionFilters.length === 0 ? '2px solid #1976d2' : 'none',
              marginBottom: 2,
              transition: 'border 0.15s'
            }}
            aria-pressed={occasionFilters.length === 0}
            title="All Occasions"
          >
            All
          </button>
          {occasionOptions.map(occasion => (
            <button
              key={occasion.key}
              onClick={() => toggleOccasion(occasion.key)}
              style={{
                padding: '6px 10px',
                borderRadius: '6px',
                border: occasionFilters.includes(occasion.key) ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                background: occasionFilters.includes(occasion.key) ? '#e3eafc' : '#fff',
                color: occasionFilters.includes(occasion.key) ? '#1976d2' : '#333',
                fontWeight: 600,
                cursor: 'pointer',
                boxShadow: occasionFilters.includes(occasion.key) ? '0 2px 8px #e3eafc' : 'none',
                outline: occasionFilters.includes(occasion.key) ? '2px solid #1976d2' : 'none',
                marginBottom: 2,
                transition: 'border 0.15s'
              }}
              aria-pressed={occasionFilters.includes(occasion.key)}
              title={occasion.label}
            >
              {occasion.label}
            </button>
          ))}
        </div>
      </div>

      <div className="outfits-grid">
        {loading ? (
          <div>Loading...</div>
        ) : filteredOutfits.length === 0 ? (
          <div style={{ textAlign: 'center', width: '100%', marginTop: 40 }}>
            No outfits found.
          </div>
        ) : (
          filteredOutfits.map(outfit => (
            <div
              key={outfit._id}
              className="outfit-card"
              style={{ textDecoration: 'none', cursor: 'pointer' }}
              onClick={() => openSidebar(outfit)}
            >
              <div className="outfit-card-img-bracket">
                {outfit.imageUrl ? (
                  <img
                    src={
                      outfit.imageUrl.startsWith('/images/')
                        ? outfit.imageUrl
                        : `/images/${outfit.imageUrl}`
                    }
                    alt="Outfit"
                  />
                ) : (
                  <div className="outfit-card-img-grid">
                    {/* ...handle multiple images or fallback... */}
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Sidebar for outfit details */}
      {sidebarOpen && sidebarOutfit && (
        <>
          {/* Overlay */}
          <div
            onClick={closeSidebar}
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              width: '100vw',
              height: '100vh',
              background: 'rgba(0,0,0,0.18)',
              zIndex: 1500
            }}
          />
          <div
            style={{
              position: 'fixed',
              top: 0,
              right: 0,
              width: '370px',
              height: '100vh',
              maxHeight: '100vh', // Ensure sidebar never exceeds viewport
              background: '#fafdff',
              boxShadow: '-4px 0 24px rgba(25, 118, 210, 0.13)',
              zIndex: 2000,
              padding: '32px 28px 24px 28px',
              overflowY: 'auto', // Enable vertical scrolling
              transition: 'right 0.2s',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center'
            }}
          >
            <button
              onClick={closeSidebar}
              style={{
                position: 'absolute',
                top: 18,
                right: 18,
                background: 'none',
                border: 'none',
                fontSize: '2em',
                color: '#1976d2',
                cursor: 'pointer'
              }}
              aria-label="Close"
            >
              ×
            </button>
            <img
              src={
                sidebarOutfit.imageUrl?.startsWith('/images/')
                  ? sidebarOutfit.imageUrl
                  : `/images/${sidebarOutfit.imageUrl}`
              }
              alt={sidebarOutfit.name}
              style={{
                width: '240px',
                height: '426px',
                objectFit: 'cover',
                borderRadius: '14px',
                boxShadow: '0 2px 12px rgba(25, 118, 210, 0.10)',
                marginBottom: '18px',
                marginTop: '18px',
                background: '#f7f7f7'
              }}
            />
            <h2 style={{
              fontSize: '1.6em',
              fontWeight: 700,
              color: '#1976d2',
              marginBottom: 10,
              textAlign: 'center'
            }}>{sidebarOutfit.name}</h2>
            <div style={{ width: '100%', marginBottom: 10 }}>
              <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Season:</div>
              <div style={{ color: '#333', marginBottom: 8 }}>{Array.isArray(sidebarOutfit.season) ? sidebarOutfit.season.join(', ') : sidebarOutfit.season}</div>
              <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Occasion:</div>
              <div style={{ color: '#333', marginBottom: 8 }}>{sidebarOutfit.occasion}</div>
            </div>
            <div style={{ width: '100%', marginBottom: 10 }}>
              <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Items in this outfit:</div>
              {sidebarOutfit.items && sidebarOutfit.items.length > 0 ? (
                sidebarOutfit.items.map((entry, idx) =>
                  entry.item ? (
                    <div
                      key={entry.item._id || idx}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 10,
                        marginBottom: 8,
                        cursor: 'pointer',
                        borderRadius: 8,
                        padding: '4px 6px',
                        background: '#f7f7f7'
                      }}
                      onClick={() => handleItemClick(entry.item._id)}
                    >
                      <img
                        src={
                          entry.item.imageUrl?.startsWith('/images/')
                            ? entry.item.imageUrl
                            : `/images/${entry.item.imageUrl}`
                        }
                        alt={entry.item.name}
                        style={{
                          width: 44,
                          height: 44,
                          objectFit: 'cover',
                          borderRadius: 8,
                          background: '#eee'
                        }}
                      />
                      <div>
                        <div style={{ fontWeight: 600, color: '#1976d2' }}>{entry.item.name}</div>
                        <div style={{ fontSize: '0.9em', color: '#555' }}>{entry.position}</div>
                      </div>
                    </div>
                  ) : null
                )
              ) : (
                <span>No clothing items</span>
              )}
            </div>
            {/* Clothing item info sidebar (nested) */}
            {sidebarItem && (
              <div
                style={{
                  position: 'fixed',
                  top: 0,
                  right: 0,
                  width: '370px',
                  height: '100vh',
                  background: '#fff',
                  boxShadow: '-4px 0 24px rgba(25, 118, 210, 0.13)',
                  zIndex: 2100,
                  padding: '32px 28px 24px 28px',
                  overflowY: 'auto',
                  transition: 'right 0.2s',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center'
                }}
              >
                <button
                  onClick={() => setSidebarItem(null)}
                  style={{
                    position: 'absolute',
                    top: 18,
                    right: 18,
                    background: 'none',
                    border: 'none',
                    fontSize: '2em',
                    color: '#1976d2',
                    cursor: 'pointer'
                  }}
                  aria-label="Close"
                >
                  ×
                </button>
                <img
                  src={
                    sidebarItem.imageUrl?.startsWith('/images/')
                      ? sidebarItem.imageUrl
                      : `/images/${sidebarItem.imageUrl}`
                  }
                  alt={sidebarItem.name}
                  style={{
                    width: '240px',
                    height: '240px',
                    objectFit: 'cover',
                    borderRadius: '14px',
                    boxShadow: '0 2px 12px rgba(25, 118, 210, 0.10)',
                    marginBottom: '18px',
                    marginTop: '18px',
                    background: '#f7f7f7'
                  }}
                />
                <h2 style={{
                  fontSize: '1.6em',
                  fontWeight: 700,
                  color: '#1976d2',
                  marginBottom: 10,
                  textAlign: 'center'
                }}>{sidebarItem.name}</h2>
                <div style={{ width: '100%', marginBottom: 10 }}>
                  <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Category:</div>
                  <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.category} {sidebarItem.subCategory ? `- ${sidebarItem.subCategory}` : ''}</div>
                  <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Size:</div>
                  <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.size}</div>
                  <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Color:</div>
                  <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.color}</div>
                  <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Season:</div>
                  <div style={{ color: '#333', marginBottom: 8 }}>{Array.isArray(sidebarItem.season) ? sidebarItem.season.join(', ') : sidebarItem.season}</div>
                  {sidebarItem.notes && (
                    <>
                      <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Notes:</div>
                      <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.notes}</div>
                    </>
                  )}
                </div>
              </div>
            )}
          </div>
        </>
      )}

      {/* Floating Add Outfit Button - always visible */}
      <button
        className="add-clothing-fab"
        onClick={() => navigate('/addOutfit')}
        aria-label="Add Outfit"
      >
        +
      </button>
    </div>
  );
}

export default OutfitList;