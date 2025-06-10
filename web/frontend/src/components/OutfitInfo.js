import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import '../DetailsCard.css';

function OutfitInfo() {
  const { id } = useParams();
  const [outfit, setOutfit] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchOutfit() {
      const res = await fetch(`/api/outfits/${id}`, { credentials: 'include' });
      const data = await res.json();
      setOutfit(data);
      setLoading(false);
    }
    fetchOutfit();
  }, [id]);

  if (loading) return <div>Loading...</div>;
  if (!outfit || outfit.message) return <div>Outfit not found.</div>;

  return (
    <div
      className="details-card"
      style={{
        background: "#fffaf6", // light peach
        borderRadius: 18,
        boxShadow: "0 4px 24px rgba(34, 70, 34, 0.10), 0 1.5px 4px rgba(255, 183, 77, 0.10)",
        padding: "32px 28px 24px 28px",
        fontFamily: "'Segoe UI', 'Roboto', Arial, sans-serif"
      }}
    >
      <div
        className="details-title"
        style={{
          color: "#225622", // dark green
          fontWeight: 700,
          fontSize: "2em",
          marginBottom: 18,
          textAlign: "center"
        }}
      >
        {outfit.name}
      </div>
      <div className="details-section">
        <h4 style={{ color: "#225622" }}>Outfit Images:</h4>
        <div className="details-img-list">
          {outfit.imageUrl ? (
            <img
              src={
                outfit.imageUrl.startsWith('/images/')
                  ? outfit.imageUrl
                  : `/images/${outfit.imageUrl}`
              }
              alt="Outfit"
              style={{
                borderRadius: 12,
                background: "#ffe5b4",
                boxShadow: "0 2px 12px rgba(34, 70, 34, 0.08)"
              }}
            />
          ) : (
            outfit.images.map((img, idx) => (
              <img
                key={idx}
                src={img.startsWith('/images/') ? img : `/images/${img}`}
                alt={`Outfit item ${idx + 1}`}
                style={{
                  borderRadius: 12,
                  background: "#ffe5b4",
                  boxShadow: "0 2px 12px rgba(34, 70, 34, 0.08)"
                }}
              />
            ))
          )}
        </div>
      </div>
      <div className="details-section">
        <h4 style={{ color: "#225622" }}>Clothing Items in this Outfit:</h4>
        <div className="details-items-list">
          {outfit.items && outfit.items.length > 0 ? (
            outfit.items.map((entry, idx) =>
              entry.item ? (
                <div
                  key={entry.item._id || idx}
                  className="details-item-card"
                  style={{
                    background: "#ffe5b4",
                    borderRadius: 8,
                    boxShadow: "0 1px 4px rgba(34, 70, 34, 0.06)",
                    color: "#225622"
                  }}
                >
                  <img
                    src={
                      entry.item.imageUrl?.startsWith('/images/')
                        ? entry.item.imageUrl
                        : `/images/${entry.item.imageUrl}`
                    }
                    alt={entry.item.name}
                    style={{
                      borderRadius: 6,
                      background: "#fffaf6"
                    }}
                  />
                  <div style={{ color: "#225622", fontWeight: 600 }}>{entry.item.name}</div>
                  <div style={{ fontSize: '0.8em', color: '#666' }}>{entry.position}</div>
                </div>
              ) : null
            )
          ) : (
            <span>No clothing items</span>
          )}
        </div>
      </div>
      <div className="details-section">
        <div className="details-meta">
          <strong style={{ color: "#225622" }}>Season:</strong>{" "}
          <span style={{ color: "#333" }}>{outfit.season && outfit.season.join(', ')}</span>
        </div>
        <div className="details-meta">
          <strong style={{ color: "#225622" }}>Occasion:</strong>{" "}
          <span style={{ color: "#333" }}>{outfit.occasion}</span>
        </div>
      </div>
    </div>
  );
}

export default OutfitInfo;