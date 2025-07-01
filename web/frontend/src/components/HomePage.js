import { useContext, useRef, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { UserContext } from "../userContext";
import { FaTshirt, FaUserCircle, FaMapMarkedAlt, FaCloudSun, FaSearch, FaCogs, FaUserPlus, FaSignInAlt, FaSignOutAlt, FaUser } from "react-icons/fa";
import { GiClothes } from "react-icons/gi";
import "../HomePage.css";

function HomePage() {
  const { user } = useContext(UserContext);
  const navigate = useNavigate();
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);
  const accountMenuRef = useRef(null);

  // Close menu when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (accountMenuRef.current && !accountMenuRef.current.contains(event.target)) {
        setAccountMenuOpen(false);
      }
    }
    if (accountMenuOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [accountMenuOpen]);

  // Account menu
  const accountMenu = (
    <div className="account-menu" ref={accountMenuRef}>
      <FaUserCircle
        size={38}
        className="account-icon"
        style={{ cursor: "pointer" }}
        onClick={() => setAccountMenuOpen((open) => !open)}
      />
      {accountMenuOpen && (
        <div className="account-dropdown">
          {!user && (
            <>
              <div onClick={() => { setAccountMenuOpen(false); navigate("/login"); }}><FaSignInAlt /> Login</div>
              <div onClick={() => { setAccountMenuOpen(false); navigate("/register"); }}><FaUserPlus /> Register</div>
            </>
          )}
          {user && (
            <>
              <div onClick={() => { setAccountMenuOpen(false); navigate("/profile"); }}><FaUser /> Profile</div>
              <div onClick={() => { setAccountMenuOpen(false); navigate("/logout"); }}><FaSignOutAlt /> Logout</div>
            </>
          )}
        </div>
      )}
    </div>
  );

  // Admin menu (unchanged)
  const adminMenu = user?.role === "admin" && (
    <div className="admin-menu">
      <FaCogs size={36} className="admin-icon" />
      <div className="admin-dropdown">
        <div onClick={() => navigate("/admin")}>Admin</div>
        <div onClick={() => navigate("/addClothingStore")}>Add Clothing Store</div>
        <div onClick={() => navigate("/addClothingStoreLocation")}>Add Clothing Store Location</div>
      </div>
    </div>
  );

  if (!user) {
    return (
      <div className="homepage-container" style={{
        minHeight: "100vh",
        background: "linear-gradient(120deg, #fffaf6 0%, #ffe5b4 100%)",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center"
      }}>
        <div className="homepage-header" style={{ width: "100%", position: "relative" }}>
          <div style={{ flex: 1 }} />
          <div style={{ position: "absolute", top: 24, right: 36 }}>{accountMenu}</div>
        </div>
        <div className="homepage-center-title" style={{ textAlign: "center", marginBottom: 24 }}>
          <h1 style={{
            fontSize: "3.2em",
            fontWeight: 500,
            color: "#225622",
            letterSpacing: "2px",
            fontFamily: "'Montserrat', 'Segoe UI', Arial, sans-serif",
            marginBottom: 10,
            textShadow: "0 2px 16px #ffe5b4"
          }}>
            Welcome to Closy
          </h1>
          <div style={{
            width: 80,
            height: 5,
            borderRadius: 3,
            margin: "0 auto 18px auto",
        
          }}></div>
          <p style={{
            fontSize: "1.25em",
            color: "#225622",
            maxWidth: 540,
            margin: "0 auto",
            fontWeight: 500,
            lineHeight: 1.6,
            background: "#fffaf6cc",
            borderRadius: 12,
            padding: "18px 28px",
            
          }}>
            Closy is your digital closet companion.<br /><br />
            <strong>What can you do?</strong>
            <ul style={{ textAlign: "left", margin: "18px auto 0 auto", maxWidth: 420, color: "#225622", fontWeight: 500 }}>
              <li>Save and organize your clothing items</li>
              <li>Create and plan your own outfits</li>
              <li>Exchange clothing items with others</li>
              <li>Post and share your favorite outfits</li>
              <li>Discover new styles and manage your wardrobe digitally</li>
            </ul>
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="homepage-container">
      <div className="homepage-header">
        <div style={{ flex: 1 }} />
        <div style={{ position: "absolute", top: 24, right: 36, display: "flex", gap: 16 }}>
          {adminMenu}
          {accountMenu}
        </div>
      </div>
      <div
        className="homepage-main-icons"
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: "32px",
          justifyContent: "center",
          alignItems: "center",
          marginTop: 48,
          marginBottom: 48
        }}
      >
        {/* First row: Map, Weather, Explore */}
        <div
          className="homepage-icon"
          onClick={() => navigate("/map")}
          style={{
            background: "#ffe5b4",
            color: "#225622",
            borderRadius: 16,
            padding: "24px 32px",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            cursor: "pointer",
            minWidth: 140,
            minHeight: 140,
            boxShadow: "0 2px 12px #ffe5b455",
            transition: "background 0.15s, transform 0.15s"
          }}
        >
          <FaMapMarkedAlt
            size="clamp(48px, 8vw, 80px)"
            style={{ color: "#225622", marginBottom: 12, width: "100%", height: "auto" }}
          />
          <div style={{
            color: "#225622",
            fontWeight: 700,
            fontSize: "1.2em",
            marginTop: 4,
            textAlign: "center"
          }}>
            Map
          </div>
        </div>
        <div
          className="homepage-icon"
          onClick={() => navigate("/weather")}
          style={{
            background: "#ffe5b4",
            color: "#225622",
            borderRadius: 16,
            padding: "24px 32px",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            cursor: "pointer",
            minWidth: 140,
            minHeight: 140,
            boxShadow: "0 2px 12px #ffe5b455",
            transition: "background 0.15s, transform 0.15s"
          }}
        >
          <FaCloudSun
            size="clamp(48px, 8vw, 80px)"
            style={{ color: "#225622", marginBottom: 12, width: "100%", height: "auto" }}
          />
          <div style={{
            color: "#225622",
            fontWeight: 700,
            fontSize: "1.2em",
            marginTop: 4,
            textAlign: "center"
          }}>
            Weather
          </div>
        </div>
        <div
          className="homepage-icon"
          onClick={() => navigate("/explore")}
          style={{
            background: "#ffe5b4",
            color: "#225622",
            borderRadius: 16,
            padding: "24px 32px",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            cursor: "pointer",
            minWidth: 140,
            minHeight: 140,
            boxShadow: "0 2px 12px #ffe5b455",
            transition: "background 0.15s, transform 0.15s"
          }}
        >
          <FaSearch
            size="clamp(48px, 8vw, 80px)"
            style={{ color: "#225622", marginBottom: 12, width: "100%", height: "auto" }}
          />
          <div style={{
            color: "#225622",
            fontWeight: 700,
            fontSize: "1.2em",
            marginTop: 4,
            textAlign: "center"
          }}>
            Explore
          </div>
        </div>
        {/* Second row: Clothing Items, Outfits */}
        <div style={{ flexBasis: "100%", height: 0 }}></div>
        <div
          className="homepage-icon"
          onClick={() => navigate("/clothingItems")}
          style={{
            background:"rgba(196, 209, 170, 0.69)",
            color: "#225622",
            borderRadius: 16,
            padding: "24px 32px",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            cursor: "pointer",
            minWidth: 140,
            minHeight: 140,
            maxWidth: 220, // Ensure consistent width
            maxHeight: 220, // Ensure consistent height
            boxShadow: "0 2px 12px #ffe5b455",
            transition: "background 0.15s, transform 0.15s"
          }}
        >
          <FaTshirt
            size="clamp(48px, 8vw, 80px)"
            style={{ color: "#225622", marginBottom: 12, width: "100%", height: "auto" }}
          />
          <div style={{
            color: "#225622",
            fontWeight: 700,
            fontSize: "1.2em",
            marginTop: 4,
            textAlign: "center"
          }}>
            Clothing Items
          </div>
        </div>
        <div
          className="homepage-icon"
          onClick={() => navigate("/outfits")}
          style={{
            background: "#ffe5b4",
            color: "#225622",
            borderRadius: 16,
            padding: "24px 32px",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            cursor: "pointer",
            minWidth: 140,
            minHeight: 140,
            maxWidth: 220,
            maxHeight: 220,
            boxShadow: "0 2px 12px #ffe5b455",
            transition: "background 0.15s, transform 0.15s"
          }}
        >
          <GiClothes
            size="clamp(48px, 8vw, 80px)"
            style={{ color: "#225622", marginBottom: 12, width: "100%", height: "auto" }}
          />
          <div style={{
            color: "#225622",
            fontWeight: 700,
            fontSize: "1.2em",
            marginTop: 4,
            textAlign: "center"
          }}>
            Outfits
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;