import { useContext, useRef, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { UserContext } from "../userContext";
import { FaTshirt, FaUserCircle, FaMapMarkedAlt, FaCloudSun, FaSearch, FaCogs, FaUserPlus, FaSignInAlt, FaSignOutAlt, FaUser } from "react-icons/fa";
import { GiClothes } from "react-icons/gi";
import { MdOutlineCheckroom } from "react-icons/md";
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
      <div className="homepage-container">
        <div className="homepage-header">
          <div style={{ flex: 1 }} />
          <div style={{ position: "absolute", top: 24, right: 36 }}>{accountMenu}</div>
        </div>
        <div className="homepage-center-title">
          <h1>Welcome to Closy</h1>
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
      <div className="homepage-main-icons">
        {/* My Closet */}
        <div className="homepage-icon-group" onClick={() => navigate("/closet")}>
          <MdOutlineCheckroom size={80} />
          <div>My Closet</div>
          <div className="homepage-sub-icons">
            <div onClick={e => { e.stopPropagation(); navigate("/clothingItems"); }}>
              <FaTshirt size={40} />
              <div>Clothing Items</div>
            </div>
            <div onClick={e => { e.stopPropagation(); navigate("/outfits"); }}>
              <GiClothes size={40} />
              <div>Outfits</div>
            </div>
          </div>
        </div>
        {/* Map */}
        <div className="homepage-icon" onClick={() => navigate("/map")}>
          <FaMapMarkedAlt size={80} />
          <div>Map</div>
        </div>
        {/* Weather */}
        <div className="homepage-icon" onClick={() => navigate("/weather")}>
          <FaCloudSun size={80} />
          <div>Weather</div>
        </div>
        {/* Explore */}
        <div className="homepage-icon" onClick={() => navigate("/explore")}>
          <FaSearch size={80} />
          <div>Explore</div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;