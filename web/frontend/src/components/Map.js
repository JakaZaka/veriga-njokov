import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { Flame } from 'lucide-react';
import { useState, useContext, useEffect, useRef } from 'react';
import { UserContext } from '../userContext';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet-routing-machine';
import React from 'react';

const storeIcon = new L.Icon({
  iconUrl: `${process.env.PUBLIC_URL}/assets/store_pin.png`,
  iconSize: [30, 40],
  iconAnchor: [15, 40],
  popupAnchor: [0, -35],
});

const userIcon = new L.Icon({
  iconUrl: `${process.env.PUBLIC_URL}/assets/user_pin.png`,
  iconSize: [25, 35],
  iconAnchor: [12, 35],
  popupAnchor: [0, -30],
});

const ResizeMapOnSidebarToggle = ({ selectedUser }) => {
  const map = useMap();
  useEffect(() => {
    setTimeout(() => {
      map.invalidateSize();
    }, 100);
  }, [selectedUser, map]);
  return null;
};

const fetchNearbyData = async ({ setStores, setUsers, radius, userContext }) => {
  const userCoordinates = userContext.user?.location?.coordinates?.coordinates || [15.6327047, 46.5610534];
  const mapCenter = [userCoordinates[1], userCoordinates[0]];
  const lat = mapCenter[0];
  const lng = mapCenter[1];
  try {
    const [storesRes, usersRes] = await Promise.all([
      fetch(`/api/locations/nearby?latitude=${lat}&longitude=${lng}&maxDistance=${radius}`),
      fetch(`/api/users/nearby?latitude=${lat}&longitude=${lng}&maxDistance=${radius}`)
    ]);
    const storesData = await storesRes.json();
    const usersData = await usersRes.json();
    setStores(Array.isArray(storesData) ? storesData : []);
    setUsers(Array.isArray(usersData) ? usersData : []);
  } catch (err) {
    console.error("Error fetching nearby data:", err);
  }
};

const fetchAllUsers = async (setUsers) => {
  try {
    const res = await fetch('/api/allWithExtras');
    const data = await res.json();
    if (data && data.data) {
      setUsers(data.data);
    } else if (Array.isArray(data)) {
      setUsers(data);
    } else {
      setUsers([]);
    }
  } catch (err) {
    console.error("Error fetching all users:", err);
    setUsers([]);
  }
};

export default function SimpleMap() {
  const userContext = useContext(UserContext);
  const [stores, setStores] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [sidebarItem, setSidebarItem] = useState(null);

  // For the slider and search
  const [radiusInput, setRadiusInput] = useState(2500); // Slider value
  const [radius, setRadius] = useState(null); // Actual search value (null = show all users)
  const [routeControl, setRouteControl] = useState(null);

  const userCoordinates = userContext.user?.location?.coordinates?.coordinates || [15.6327047, 46.5610534];
  const mapCenter = [userCoordinates[1], userCoordinates[0]];

  const [categoryFilters, setCategoryFilters] = useState({
    tops: false,
    bottoms: false,
    shoes: false,
    outerwear: false,
    accessories: false,
    dresses: false,
  });

  const clothingIcons = {
    tops: "👕",
    bottoms: "👖",
    shoes: "👟",
    outerwear: "🧥",
    accessories: "🧢",
    dresses: "👗",
  };

  const [colorFilters, setColorFilters] = useState([]);
  const [sizeFilters, setSizeFilters] = useState([]);
  const [seasonFilters, setSeasonFilters] = useState([]);

  const basicColors = [
    "Black", "White", "Gray", "Red", "Blue", "Green", "Yellow", "Pink", "Purple", "Brown", "Beige", "Orange"
  ];
  const fixedSizes = [
    "XS", "S", "M", "L", "XL", "2XL", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46"
  ];
  const seasonOptions = [
    { key: "spring", icon: "🌸", label: "Spring" },
    { key: "summer", icon: "☀️", label: "Summer" },
    { key: "fall", icon: "🍂", label: "Fall" },
    { key: "winter", icon: "❄️", label: "Winter" },
    { key: "all", icon: "🌀", label: "All" }
  ];

  // Handlers for filters
  const toggleColor = (color) => {
    setColorFilters(prev =>
      prev.includes(color)
        ? prev.filter(c => c !== color)
        : [...prev, color]
    );
  };
  const clearColorFilters = () => setColorFilters([]);
  const toggleSize = (size) => {
    setSizeFilters(prev =>
      prev.includes(size)
        ? prev.filter(s => s !== size)
        : [...prev, size]
    );
  };
  const clearSizeFilters = () => setSizeFilters([]);
  const toggleSeason = (seasonKey) => {
    setSeasonFilters(prev =>
      prev.includes(seasonKey)
        ? prev.filter(s => s !== seasonKey)
        : [...prev, seasonKey]
    );
  };
  const clearSeasonFilters = () => setSeasonFilters([]);

  // On initial load, fetch all users and all stores (with a very large radius)
  useEffect(() => {
    fetchAllUsers(setUsers);
    fetchNearbyData({ setStores, setUsers: () => {}, radius: 100000, userContext });
  }, [userContext]);

  // When radius is set (by clicking Search), fetch by radius
  useEffect(() => {
    if (radius !== null) {
      fetchNearbyData({ setStores, setUsers, radius, userContext });
    }
  }, [radius, userContext]);

  const handleLikeOutfit = async (outfitId) => {
    try {
      const token = localStorage.getItem('token');
      const res = await fetch(`/api/outfits/${outfitId}/like`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
      });
      if (!res.ok) throw new Error('Failed to toggle like');
      // Refresh the page after like
      window.location.reload();
    } catch (err) {
      console.error('Error toggling like:', err);
    }
  };

  const drawRouteTo = (destinationCoordinates) => {
    if (!userContext.user?.location?.coordinates?.coordinates) return;
    const userCoords = userContext.user.location.coordinates.coordinates;
    const from = L.latLng(userCoords[1], userCoords[0]);
    const to = L.latLng(destinationCoordinates[1], destinationCoordinates[0]);
    if (routeControl) {
      routeControl.remove();
    }
    const control = L.Routing.control({
      waypoints: [from, to],
      lineOptions: {
        styles: [{ color: 'rgb(126, 244, 240)', weight: 5 }],
      },
      router: L.Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1',
      }),
      show: false,
      addWaypoints: false,
      draggableWaypoints: false,
      fitSelectedRoutes: true,
      createMarker: () => null,
      routeWhileDragging: false,
      containerClassName: 'hidden-routing-container',
    }).addTo(mapRef.current);
    setRouteControl(control);
  };

  useEffect(() => {
    const style = document.createElement('style');
    style.innerHTML = `
      .hidden-routing-container {
        display: none !important;
      }
    `;
    document.head.appendChild(style);
    return () => {
      document.head.removeChild(style);
    };
  }, []);

  const mapRef = useRef();

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', width: '100%' }}>
      <div style={{ display: 'flex', width: '100%', marginBottom: 8 }}>
        <div style={{ margin: '8px 0', marginRight: 32 }}>
          <label>
            Radius: {(radiusInput / 1000).toFixed(1)} km
            <input
              type="range"
              min="500"
              max="100000"
              step="100"
              value={radiusInput}
              onChange={e => setRadiusInput(Number(e.target.value))}
                style={{
                  marginLeft: '18px',
                  width: '300px',
                  verticalAlign: 'middle',
                  accentColor: '#225622', // dark green for the dragger/thumb
                  background: '#ffe5b4',  // light peach for the track (supported in modern browsers)
                  height: '6px',
                  borderRadius: '4px'
                }}
            />
          </label>
          <button
            style={{
              marginLeft: 12,
              padding: "6px 18px",
              borderRadius: 6,
              background: "#225622",
              color: "#fff",
              border: "none",
              fontWeight: 600,
              cursor: "pointer"
            }}
            onClick={() => setRadius(radiusInput)}
          >
            Search
          </button>
        </div>
        <div style={{ marginBottom: '8px' }}>
          <strong>Filter by clothing type:</strong>
          <div style={{ display: 'flex', gap: '12px', marginTop: '6px', flexWrap: 'wrap' }}>
            {Object.keys(categoryFilters).map((category) => (
              <button
                key={category}
                onClick={() =>
                  setCategoryFilters(prev => ({
                    ...prev,
                    [category]: !prev[category]
                  }))
                }
                style={{
                  padding: '6px 10px',
                  borderRadius: '6px',
                  border: categoryFilters[category] ? '2px solid #225622' : '1px solid #ccc',
                  backgroundColor: categoryFilters[category] ? '#ffe5b4' : '#fff',
                  color: categoryFilters[category] ? '#225622' : '#333',
                  cursor: 'pointer',
                  fontSize: '18px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  fontWeight: 600,
                  boxShadow: categoryFilters[category] ? '0 2px 8px #ffe5b4' : 'none'
                }}
                aria-pressed={categoryFilters[category]}
              >
                <span>{clothingIcons[category]}</span>
                <span>{category.charAt(0).toUpperCase() + category.slice(1)}</span>
              </button>
            ))}
          </div>
          {/* Color Filters */}
          <div style={{ display: 'flex', gap: '18px', margin: '12px 0', flexWrap: 'wrap', alignItems: 'center' }}>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <button
                onClick={clearColorFilters}
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: '6px',
                  border: colorFilters.length === 0 ? '2.5px solid #225622' : '1.5px solid #ccc',
                  background: '#ffe5b4',
                  color: 'transparent',
                  cursor: 'pointer',
                  boxShadow: colorFilters.length === 0 ? '0 2px 8px #ffe5b4' : 'none',
                  outline: colorFilters.length === 0 ? '2px solid #225622' : 'none',
                  marginBottom: 2,
                  transition: 'border 0.15s'
                }}
                aria-pressed={colorFilters.length === 0}
                title="All Colors"
              >
                &nbsp;
              </button>
              <span style={{
                fontSize: '0.95em',
                color: '#225622',
                marginTop: 2,
                textAlign: 'center'
              }}>
                All
              </span>
            </div>
            {basicColors.map(color => (
              <div key={color} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <button
                  onClick={() => toggleColor(color)}
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: '6px',
                    border: colorFilters.includes(color) ? '2.5px solid #225622' : '1.5px solid #ccc',
                    background: color.toLowerCase(),
                    color: 'transparent',
                    cursor: 'pointer',
                    boxShadow: colorFilters.includes(color) ? '0 2px 8px #ffe5b4' : 'none',
                    outline: colorFilters.includes(color) ? '2px solid #225622' : 'none',
                    marginBottom: 2,
                    transition: 'border 0.15s'
                  }}
                  aria-pressed={colorFilters.includes(color)}
                  title={color}
                >
                  &nbsp;
                </button>
                <span style={{
                  fontSize: '0.95em',
                  color: '#225622',
                  marginTop: 2,
                  textAlign: 'center'
                }}>
                  {color}
                </span>
              </div>
            ))}
          </div>
          {/* Size Filters */}
          <div style={{ display: 'flex', gap: '14px', marginBottom: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <button
                onClick={clearSizeFilters}
                style={{
                  minWidth: 36,
                  minHeight: 36,
                  borderRadius: '6px',
                  border: sizeFilters.length === 0 ? '2.5px solid #225622' : '1.5px solid #ccc',
                  background: '#ffe5b4',
                  color: 'transparent',
                  cursor: 'pointer',
                  boxShadow: sizeFilters.length === 0 ? '0 2px 8px #ffe5b4' : 'none',
                  outline: sizeFilters.length === 0 ? '2px solid #225622' : 'none',
                  marginBottom: 2,
                  fontWeight: 600,
                  fontSize: '1em',
                  transition: 'border 0.15s'
                }}
                aria-pressed={sizeFilters.length === 0}
                title="All Sizes"
              >
                &nbsp;
              </button>
              <span style={{
                fontSize: '0.95em',
                color: '#225622',
                marginTop: 2,
                textAlign: 'center'
              }}>
                All
              </span>
            </div>
            {fixedSizes.map(size => (
              <div key={size} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <button
                  onClick={() => toggleSize(size)}
                  style={{
                    minWidth: 36,
                    minHeight: 36,
                    borderRadius: '6px',
                    border: sizeFilters.includes(size) ? '2.5px solid #225622' : '1.5px solid #ccc',
                    background: sizeFilters.includes(size) ? '#ffe5b4' : '#fff',
                    color: '#333',
                    cursor: 'pointer',
                    boxShadow: sizeFilters.includes(size) ? '0 2px 8px #ffe5b4' : 'none',
                    outline: sizeFilters.includes(size) ? '2px solid #225622' : 'none',
                    marginBottom: 2,
                    fontWeight: 600,
                    fontSize: '1em',
                    transition: 'border 0.15s'
                  }}
                  aria-pressed={sizeFilters.includes(size)}
                  title={size}
                >
                  {size}
                </button>
              </div>
            ))}
          </div>
          {/* Season Filters */}
          <div style={{ display: 'flex', gap: '14px', marginBottom: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <button
                onClick={clearSeasonFilters}
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: '6px',
                  border: seasonFilters.length === 0 ? '2.5px solid #225622' : '1.5px solid #ccc',
                  background: '#ffe5b4',
                  color: 'transparent',
                  cursor: 'pointer',
                  boxShadow: seasonFilters.length === 0 ? '0 2px 8px #ffe5b4' : 'none',
                  outline: seasonFilters.length === 0 ? '2px solid #225622' : 'none',
                  marginBottom: 2,
                  transition: 'border 0.15s'
                }}
                aria-pressed={seasonFilters.length === 0}
                title="All Seasons"
              >
                &nbsp;
              </button>
              <span style={{
                fontSize: '0.95em',
                color: '#225622',
                marginTop: 4,
                textAlign: 'center'
              }}>
                All
              </span>
            </div>
            {seasonOptions.map(season => (
              <div key={season.key} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <button
                  onClick={() => toggleSeason(season.key)}
                  style={{
                    width: 56,
                    height: 56,
                    borderRadius: '12px',
                    border: seasonFilters.includes(season.key) ? '2.5px solid #225622' : '1.5px solid #ccc',
                    background: '#fff',
                    color: '#222',
                    cursor: 'pointer',
                    boxShadow: seasonFilters.includes(season.key) ? '0 2px 8px #ffe5b4' : 'none',
                    outline: seasonFilters.includes(season.key) ? '2px solid #225622' : 'none',
                    marginBottom: 2,
                    fontSize: '2.6em',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    transition: 'border 0.15s',
                    position: 'relative',
                    overflow: 'hidden'
                  }}
                  aria-pressed={seasonFilters.includes(season.key)}
                  title={season.label}
                >
                  <span style={{
                    display: 'block',
                    position: 'relative',
                    lineHeight: 1
                  }}>
                    {season.icon}
                  </span>
                </button>
                <span style={{
                  fontSize: '0.95em',
                  color: '#225622',
                  marginTop: 1,
                  textAlign: 'center'
                }}>
                  {season.label}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
      <div style={{ display: 'flex', flex: 1, minHeight: 0 }}>
        {/* Map Section */}
        <div style={{ flex: 2, border: '1px solid #ccc', borderRadius: '8px', overflow: 'hidden', minHeight: 0 }}>
          <MapContainer
            center={mapCenter}
            zoom={15}
            ref={mapRef}
            style={{ height: '100%', width: '100%' }}
            whenReady={() => {
              if (radius !== null) {
                fetchNearbyData({ setStores, setUsers, radius, userContext });
              } else {
                fetchAllUsers(setUsers);
                fetchNearbyData({ setStores, setUsers: () => {}, radius: 100000, userContext });
              }
            }}
          >
            <ResizeMapOnSidebarToggle selectedUser={selectedUser} />
            <TileLayer
              attribution='&copy; OpenStreetMap contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {stores
              .filter(store => Array.isArray(store.coordinates.coordinates) &&
                store.coordinates.coordinates.length === 2 &&
                !isNaN(store.coordinates.coordinates[0]) &&
                !isNaN(store.coordinates.coordinates[1]))
              .map(store => (
                <Marker
                  key={`store-${store._id}`}
                  position={[store.coordinates.coordinates[1], store.coordinates.coordinates[0]]}
                  icon={storeIcon}
                  eventHandlers={{
                    popupclose: () => {
                      if (routeControl) {
                        routeControl.remove();
                        setRouteControl(null);
                      }
                    }
                  }}
                >
                  <Popup maxWidth={250}>
                    <div>
                      <h3>{store.clothingStoreId?.name || 'Unknown Store'}</h3>
                      <p>{store.address}, {store.city}</p>
                      {store.clothingStoreId?.website && (
                        <a href={store.clothingStoreId.website} target="_blank" rel="noopener noreferrer">
                          Visit Website
                        </a>
                      )}
                      <button
                        onClick={() => drawRouteTo(store.coordinates.coordinates)}
                        style={{ marginTop: '8px', cursor: 'pointer', fontSize: '1.2em' }}
                        title="Get directions to store"
                      >
                        🚗
                      </button>
                    </div>
                  </Popup>
                </Marker>
              ))}

            {users
              .filter(user => {
                const coords = user.location?.coordinates?.coordinates;
                if (!Array.isArray(coords) || coords.length !== 2) return false;

                const activeCategories = Object.entries(categoryFilters)
                  .filter(([_, val]) => val)
                  .map(([key]) => key.toLowerCase());

                const isCurrentUser = user._id === userContext.user?._id;

                // --- Clothing filters ---
                const matchesCategory =
                  activeCategories.length === 0 ||
                  user.clothesForSale?.some(item =>
                    item.category && activeCategories.includes(item.category.toLowerCase())
                  );

                const matchesColor =
                  colorFilters.length === 0 ||
                  user.clothesForSale?.some(item =>
                    item.color && colorFilters.includes(item.color)
                  );

                const matchesSize =
                  sizeFilters.length === 0 ||
                  user.clothesForSale?.some(item =>
                    item.size && sizeFilters.includes(item.size)
                  );

                const matchesSeason =
                  seasonFilters.length === 0 ||
                  user.clothesForSale?.some(item =>
                    item.season && (
                      Array.isArray(item.season)
                        ? item.season.some(s => seasonFilters.includes(s))
                        : seasonFilters.includes(item.season)
                    )
                  );

                if (isCurrentUser) return true;
                return matchesCategory && matchesColor && matchesSize && matchesSeason;
              })
              .map(user => {
                const isCurrentUser = user._id === userContext.user?._id;
                return (
                  <Marker
                    key={`user-${user._id}`}
                    position={[user.location.coordinates.coordinates[1], user.location.coordinates.coordinates[0]]}
                    icon={userIcon}
                    eventHandlers={{
                      click: () => {
                        if (!isCurrentUser) {
                          setSelectedUser(user);
                          setSidebarItem(null);
                        }
                      },
                      popupclose: () => {
                        if (routeControl) {
                          routeControl.remove();
                          setRouteControl(null);
                        }
                      }
                    }}
                  >
                    <Popup maxWidth={300}>
                      <div>
                        <h3>{isCurrentUser ? 'My Closet' : user.username}</h3>
                        {!isCurrentUser && (
                          <>
                            <p>{user.email}</p>
                            <p>{user.location.address}</p>
                            <button
                              onClick={() => drawRouteTo(user.location.coordinates.coordinates)}
                              style={{ marginTop: '8px', cursor: 'pointer', fontSize: '1.2em' }}
                              title="Get driving directions"
                            >
                              🚗
                            </button>
                          </>
                        )}
                      </div>
                    </Popup>
                  </Marker>
                );
              })}
          </MapContainer>
        </div>

        {/* Sidebar */}
        {selectedUser && (
          <div
            style={{
              flex: 1,
              marginLeft: '16px',
              padding: '18px 12px 12px 12px',
              backgroundColor: '#fffaf6',
              border: '1px solid #ffe5b4',
              borderRadius: '8px',
              overflowY: 'auto',
              minWidth: 320,
              maxWidth: 400,
              boxShadow: '0 2px 12px rgba(34, 86, 34, 0.10)',
              position: 'relative',
              display: 'flex',
              flexDirection: 'column',
              gap: 18
            }}
          >
            <button onClick={() => setSelectedUser(null)} style={{ float: 'right', cursor: 'pointer', fontSize: 22, background: 'none', border: 'none', color: '#225622' }}>
              ✖
            </button>
            <h2 style={{ fontWeight: 700, color: '#225622', margin: '0 0 12px 0', fontSize: '1.5em' }}>
              {selectedUser.username}
            </h2>

            {/* Clothes for Sale - Want to Give */}
            <div>
              <h4 style={{ color: '#225622', marginBottom: 8, fontWeight: 600 }}>Clothes to Give</h4>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                {selectedUser.clothesForSale?.filter(item => item.wantToGive).length > 0 ? (
                  selectedUser.clothesForSale
                    .filter(item => item.wantToGive)
                    .map((item, idx) => (
                      <div
                        key={item._id || idx}
                        style={{
                          width: 64,
                          textAlign: 'center',
                          cursor: 'pointer',
                          borderRadius: 8,
                          background: '#fff',
                          boxShadow: '0 2px 8px #ffe5b433',
                          padding: 4,
                          border: '1px solid #ffe5b4',
                          position: 'relative'
                        }}
                        onClick={() => setSidebarItem({ ...item, _sidebarType: 'clothing' })}
                        title={item.name}
                      >
                        <img
                          src={item.imageUrl?.startsWith('/images/') ? item.imageUrl : `/images/${item.imageUrl}`}
                          alt={item.name}
                          style={{
                            width: 48,
                            height: 48,
                            objectFit: 'cover',
                            borderRadius: 6,
                            background: '#f7f7f7',
                            marginBottom: 4
                          }}
                        />
                        <div style={{ fontSize: '0.92em', color: '#225622', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {item.name}
                        </div>
                        <div style={{ fontSize: '0.8em', color: '#555' }}>{item.category}</div>
                      </div>
                    ))
                ) : (
                  <div style={{ color: '#888', fontSize: '0.98em' }}>No items</div>
                )}
              </div>
            </div>

            {/* Outfits */}
            <div>
              <h4 style={{ color: '#225622', margin: '18px 0 8px 0', fontWeight: 600 }}>Outfits</h4>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                {selectedUser.outfits?.length > 0 ? (
                  selectedUser.outfits.map((outfit, idx) => (
                    <div
                      key={outfit._id || idx}
                      style={{
                        width: 64,
                        textAlign: 'center',
                        cursor: 'pointer',
                        borderRadius: 8,
                        background: '#fff',
                        boxShadow: '0 2px 8px #ffe5b433',
                        padding: 4,
                        border: '1px solid #ffe5b4',
                        position: 'relative'
                      }}
                      onClick={() => setSidebarItem({ ...outfit, _sidebarType: 'outfit' })}
                      title={outfit.name}
                    >
                      <img
                        src={
                          outfit.imageUrl
                            ? (outfit.imageUrl.startsWith('/images/') ? outfit.imageUrl : `/images/${outfit.imageUrl}`)
                            : (outfit.images && outfit.images.length > 0
                              ? (outfit.images[0].startsWith('/images/') ? outfit.images[0] : `/images/${outfit.images[0]}`)
                              : '/images/no-image.png')
                        }
                        alt={outfit.name}
                        style={{
                          width: 48,
                          height: 48,
                          objectFit: 'cover',
                          borderRadius: 6,
                          background: '#f7f7f7',
                          marginBottom: 4
                        }}
                      />
                      <div style={{ fontSize: '0.92em', color: '#225622', fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {outfit.name}
                      </div>
                      <div style={{ fontSize: '0.8em', color: '#555' }}>{outfit.items?.length || 0} items</div>
                    </div>
                  ))
                ) : (
                  <div style={{ color: '#888', fontSize: '0.98em' }}>No outfits</div>
                )}
              </div>
            </div>

            {/* Nested sidebar for clothing/outfit info */}
            {sidebarItem && (
              <div
                style={{
                  position: 'fixed',
                  top: 0,
                  right: 0,
                  width: 370,
                  height: '100vh',
                  background: '#fffaf6',
                  boxShadow: '-4px 0 24px rgba(34, 86, 34, 0.13)',
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
                    color: '#225622',
                    cursor: 'pointer',
                    zIndex: 2200 // ensure above content
                  }}
                  aria-label="Close"
                >
                  ×
                </button>
                {/* Clothing Item Info */}
                {sidebarItem._sidebarType === 'clothing' && (
                  <>
                    <img
                      src={sidebarItem.imageUrl?.startsWith('/images/') ? sidebarItem.imageUrl : `/images/${sidebarItem.imageUrl}`}
                      alt={sidebarItem.name}
                      style={{
                        width: '180px',
                        height: '180px',
                        objectFit: 'cover',
                        borderRadius: '14px',
                        boxShadow: '0 2px 12px rgba(34, 86, 34, 0.10)',
                        marginBottom: '18px',
                        marginTop: '18px',
                        background: '#f7f7f7'
                      }}
                    />
                    <h2 style={{
                      fontSize: '1.3em',
                      fontWeight: 700,
                      color: '#225622',
                      marginBottom: 10,
                      textAlign: 'center'
                    }}>{sidebarItem.name}</h2>
                    <div style={{ width: '100%', marginBottom: 10 }}>
                      <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Category:</div>
                      <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.category} {sidebarItem.subCategory ? `- ${sidebarItem.subCategory}` : ''}</div>
                      <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Size:</div>
                      <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.size}</div>
                      <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Color:</div>
                      <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.color}</div>
                      <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Season:</div>
                      <div style={{ color: '#333', marginBottom: 8 }}>{Array.isArray(sidebarItem.season) ? sidebarItem.season.join(', ') : sidebarItem.season}</div>
                      {sidebarItem.notes && (
                        <>
                          <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Notes:</div>
                          <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.notes}</div>
                        </>
                      )}
                    </div>
                  </>
                )}
                {/* Outfit Info */}
                {sidebarItem._sidebarType === 'outfit' && (
                  <>
                    <div
                      style={{
                        width: '240px',
                        height: '426px', // 9:16 ratio
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        marginBottom: '18px',
                        marginTop: '18px',
                        background: '#f7f7f7',
                        borderRadius: '14px',
                        boxShadow: '0 2px 12px rgba(34, 86, 34, 0.10)',
                        overflow: 'hidden'
                      }}
                    >
                      <img
                        src={
                          sidebarItem.imageUrl
                            ? (sidebarItem.imageUrl.startsWith('/images/') ? sidebarItem.imageUrl : `/images/${sidebarItem.imageUrl}`)
                            : (sidebarItem.images && sidebarItem.images.length > 0
                              ? (sidebarItem.images[0].startsWith('/images/') ? sidebarItem.images[0] : `/images/${sidebarItem.images[0]}`)
                              : '/images/no-image.png')
                        }
                        alt={sidebarItem.name}
                        style={{
                          width: '100%',
                          height: '100%',
                          objectFit: 'cover',
                          borderRadius: '14px',
                          background: '#f7f7f7'
                        }}
                      />
                    </div>
                    <h2 style={{
                      fontSize: '1.3em',
                      fontWeight: 700,
                      color: '#225622',
                      marginBottom: 10,
                      textAlign: 'center'
                    }}>{sidebarItem.name}</h2>
                    {/* Like button */}
                    <button
                      onClick={() => handleLikeOutfit(sidebarItem._id)}
                      style={{
                        background: sidebarItem.likedBy && sidebarItem.likedBy.includes(userContext.user?._id) ? '#225622' : '#fff',
                        color: sidebarItem.likedBy && sidebarItem.likedBy.includes(userContext.user?._id) ? '#fff' : '#225622',
                        border: '2px solid #225622',
                        borderRadius: 8,
                        padding: '6px 18px',
                        fontWeight: 600,
                        fontSize: '1em',
                        marginBottom: 12,
                        cursor: 'pointer',
                        marginTop: 0,
                        transition: 'background 0.15s, color 0.15s'
                      }}
                    >
                      {sidebarItem.likedBy && sidebarItem.likedBy.includes(userContext.user?._id) ? '♥ Liked' : '♡ Like'}{' '}
                      <span style={{ fontWeight: 500, marginLeft: 4 }}>
                        {sidebarItem.likedBy ? sidebarItem.likedBy.length : 0}
                      </span>
                    </button>
                    <div style={{ width: '100%', marginBottom: 10 }}>
                      <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Season:</div>
                      <div style={{ color: '#333', marginBottom: 8 }}>{Array.isArray(sidebarItem.season) ? sidebarItem.season.join(', ') : sidebarItem.season}</div>
                      <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Items in this outfit:</div>
                      {sidebarItem.items && sidebarItem.items.length > 0 ? (
                        sidebarItem.items.map((entry, idx) =>
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
                              onClick={() => setSidebarItem({ ...entry.item, _sidebarType: 'clothing' })}
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
                                <div style={{ fontWeight: 600, color: '#225622' }}>{entry.item.name}</div>
                                <div style={{ fontSize: '0.9em', color: '#555' }}>{entry.position}</div>
                              </div>
                            </div>
                          ) : null
                        )
                      ) : (
                        <span>No clothing items</span>
                      )}
                    </div>
                  </>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}