import { MapContainer, TileLayer, useMapEvents, Marker, Popup, useMap } from 'react-leaflet';
import { Flame, FlameOff } from 'lucide-react';
import { useState, useContext } from 'react';
import {UserContext} from '../userContext';
import L from 'leaflet';
import { useEffect } from 'react';
import 'leaflet-routing-machine';
import React from 'react';


//import storePin from '../public/assets/store_pin.png';
//import userPin from '../public/assets/user_pin.png';


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

const ResizeMapOnSidebarToggle = ({selectedUser}) => {
  const map = useMap();
  useEffect(() => {
    setTimeout(() => {
      map.invalidateSize();
    }, 100); 
  }, [selectedUser, map]);
  return null;
};



const LocationFetcher = ({ setStores, setUsers, radius, userContext }) => {
  const map = useMapEvents({
   moveend: () => fetchNearbyData({ setStores, setUsers, radius, userContext }),
    
  });

  return null;
   
};

 const fetchNearbyData = async ({ setStores, setUsers, radius, userContext }) => {
  const userCoordinates = userContext.user?.location?.coordinates?.coordinates || [15.6327047, 46.5610534];
  const mapCenter = [userCoordinates[1], userCoordinates[0]]; // Leaflet uses [lat, lng]
   const center = mapCenter;
      console.log('Fetching locations for center:', center, 'with radius:', radius);
      const lat = center[0];
      const lng = center[1]; 
      
      console.log('Latitude:', lat, 'Longitude:', lng);
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
  }

 

export default function SimpleMap() {
  const userContext = useContext(UserContext);
  const [stores, setStores] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [radius, setRadius] = useState(2500); // Default radius of 5km
  const [routeControl, setRouteControl] = useState(null);

  console.log('User context:', userContext.user);
  const userCoordinates = userContext.user?.location?.coordinates?.coordinates || [15.6327047, 46.5610534];
  const mapCenter = [userCoordinates[1], userCoordinates[0]]; // Leaflet uses [lat, lng]
  console.log('Map center:', mapCenter);

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
  dresses:"👗",
};



  
  
  const handleLikeOutfit = async (outfitId) => {
    try {
      //console.log('Toggling like for outfit:', outfitId);
      //console.log('Current user:', userContext.user._id);

      const res = await fetch(`/api/outfits/${outfitId}/like`, {
        method: 'POST',
        credentials: 'include',
      });
      if (!res.ok) throw new Error('Failed to toggle like');

    const updatedOutfit = await res.json();

    // Update selectedUser in local state
    setSelectedUser((prev) => ({
      ...prev,
      outfits: prev.outfits.map((o) =>
        o._id === outfitId ? updatedOutfit : o
      ),
    }));
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

  const mapRef = React.useRef();

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '550px', width: '100%' }}>
      <div style={{display: 'flex', width: '100%'}}>
      <div style={{margin: '8px 0'}}>
        <label>
          Radius: {(radius)} m
          <input
            type="range"
            min="500"
            max="5000"
            step="100"
            value={radius}
             onChange={(e) => {
                const newRadius = Number(e.target.value);
                setRadius(newRadius);
                fetchNearbyData({ setStores, setUsers, radius: newRadius, userContext });
              }}
            style={{ marginLeft: '10px', width: '300px' }}
          />
        </label>
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
                border: categoryFilters[category] ? '2px solid #333' : '1px solid #ccc',
                backgroundColor: categoryFilters[category] ? '#eee' : '#fff',
                cursor: 'pointer',
                fontSize: '18px',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
              aria-pressed={categoryFilters[category]}
            >
              <span>{clothingIcons[category]}</span>
              <span>{category.charAt(0).toUpperCase() + category.slice(1)}</span>
            </button>
              ))}
          </div>
        </div>
    </div>
   <div style={{ display: 'flex', flex: 1}}>
  {/* Map Section */}
  <div style={{ flex: 2, border: '1px solid #ccc', borderRadius: '8px', overflow: 'hidden' }}>
    <MapContainer
      center={mapCenter}
      zoom={15}
      ref={mapRef}
      whenReady={(map) => {
      const lat = mapCenter[0];
      const lng = mapCenter[1];
      fetchNearbyData({setStores, setUsers, radius, userContext}); 
  }}
      style={{ height: '100%', width: '100%' }}
    >
      <ResizeMapOnSidebarToggle selectedUser={selectedUser} />
      <LocationFetcher setStores={setStores} setUsers={setUsers} radius={radius} userContext={userContext}/>

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

            const activeFilters = Object.entries(categoryFilters)
              .filter(([_, val]) => val)
              .map(([key]) => key.toLowerCase());

            const isCurrentUser = user._id === userContext.user?._id;

            if (activeFilters.length === 0) return true;

            
            if (isCurrentUser) return true;
            return user.clothesForSale?.some(item =>
              item.category && activeFilters.includes(item.category.toLowerCase())
            );
          })

        .map(user => {
         
          const isCurrentUser = user._id === userContext.user?._id;
         return( <Marker
            key={`user-${user._id}`}
            position={[user.location.coordinates.coordinates[1], user.location.coordinates.coordinates[0]]}
            icon={userIcon}
            eventHandlers={{
                click: () => {
                  if (!isCurrentUser) {
                    setSelectedUser(user); 
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
                    <p>{/*user.contactInfo.phoneNumber*/}</p>
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
        padding: '12px',
        backgroundColor: '#fafafa',
        border: '1px solid #ccc',
        borderRadius: '8px',
        overflowY: 'auto'
      }}
    >
      <button onClick={() => {
        setSelectedUser(null)
        
      }} style={{ float: 'right', cursor: 'pointer' }}>
        ✖
      </button>
      <h1>{selectedUser.username}</h1>

<h2>Outfits:</h2>
<div>
  {selectedUser.outfits?.filter(outfit => {
    // Defensive: fallback to selectedUser._id if outfit.user is missing
    let outfitUserId;
    if (outfit.user) {
      outfitUserId = typeof outfit.user === 'object' && outfit.user !== null
        ? outfit.user._id
        : outfit.user;
    } else {
      outfitUserId = selectedUser._id;
    }
    return outfitUserId && selectedUser._id && String(outfitUserId) === String(selectedUser._id);
  }).length > 0 ? (
    selectedUser.outfits
      .filter(outfit => {
        let outfitUserId;
        if (outfit.user) {
          outfitUserId = typeof outfit.user === 'object' && outfit.user !== null
            ? outfit.user._id
            : outfit.user;
        } else {
          outfitUserId = selectedUser._id;
        }
        return outfitUserId && selectedUser._id && String(outfitUserId) === String(selectedUser._id);
      })
      .map((outfit, index) => (
        <div
          key={index}
          style={{
            border: '1px solid #ccc',
            padding: '8px',
            borderRadius: '8px',
            width: '100%',
            marginBottom: '12px',
            boxSizing: 'border-box',
            position: 'relative'
          }}
        >
          {/* Heart button */}
          <button
            onClick={() => handleLikeOutfit(outfit._id)}
            style={{
              position: 'absolute',
              top: '8px',
              right: '8px',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              fontSize: '20px',
            }}
            aria-label="Like outfit"
          >
            {outfit.likedBy?.includes(userContext.user?._id) ? (
              <Flame color="orangered" fill="orangered" />
            ) : (
              <Flame />
            )}
          </button>

          <h3 style={{ marginBottom: '6px' }}>{outfit.name}</h3>
          {outfit.images && outfit.images.length > 0 && (
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gridTemplateRows: 'repeat(2, 1fr)',
                gap: '2px',
                width: '100%',
                borderRadius: '4px'
              }}
            >
              {outfit.images.slice(0, 4).map((img, idx) => (
                <img
                  key={idx}
                  src={img.startsWith('/images/') ? img : `/images/${img}`}
                  alt={`Outfit ${idx + 1}`}
                  style={{
                    width: '100%',
                    aspectRatio: '1 / 1',
                    objectFit: 'cover',
                    background: '#eee',
                    borderRadius: '2px'
                  }}
                />
              ))}
            </div>
          )}
          <p style={{ margin: '6px 0', fontSize: '0.85em', color: '#666' }}>
            {outfit.liked} 🔥
          </p>
        </div>
      ))
  ) : (
    <div>No outfits found.</div>
  )}
</div>

<h4>Clothes for Sale:</h4>
<div>
  {selectedUser.clothesForSale?.length > 0 ? (
    selectedUser.clothesForSale.map((item, index) => (
      <div
        key={index}
        style={{
          border: '1px solid #ccc',
          borderRadius: '8px',
          padding: '8px',
          width: '100%',
          marginBottom: '12px',
          boxSizing: 'border-box'
        }}
      >
        {item.imageUrl && (
          <img
            src={item.imageUrl.startsWith('/images/') ? item.imageUrl : `/images/${item.imageUrl}`}
            alt={item.name}
            style={{
              width: '100%',
              height: '100px',
              objectFit: 'cover',
              borderRadius: '4px',
              marginBottom: '6px',
              background: '#f0f0f0'
            }}
          />
        )}
        <h5 style={{ margin: 0 }}>{item.name}</h5>
        <p style={{ margin: 0, fontSize: '0.85em', color: '#666' }}>{item.category}</p>
      </div>
    ))
  ) : (
    <p>No items</p>
  )}
</div>
    </div>
  )}
</div>
</div>

  );
}
