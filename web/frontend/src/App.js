import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Register from './components/Register';
import Login from './components/Login';
import Logout from './components/Logout';
import Profile from './components/Profile';
import Header from './components/Header';
import ClothingItems from './components/ClothingItems';
import AddClothingItem from './components/AddClothingItem';
import ClothingItemInfo from './components/ClothingItemInfo';
import AddOutfit from './components/AddOutfit';
import OutfitInfo from './components/OutfitInfo';
import ClothingStores from './components/ClothingStores';
import AddClothingStore from './components/AddClothingStore';
import AddClothingStoreLocation from './components/AddClothingStoreLocation';
import { UserContext } from './userContext';
import OutfitList from './components/OutfitList';
import ExploreClothingItems from './components/ExploreClothingItems';
import WeatherTab from './components/WeatherTab';
import AdminDashboard from "./components/AdminDashboard";
import 'bootstrap-icons/font/bootstrap-icons.css';
import Map from "./components/Map.js"

function App() {
  const [user, setUser] = useState(localStorage.user ? JSON.parse(localStorage.user) : null);

  const updateUserData = (userInfo) => {
    if (!userInfo) {
      localStorage.removeItem("user");
      setUser(null);
      return;
    }
    // Only store minimal info, not the whole profile with base64 avatar
    const minimalUser = {
      _id: userInfo._id,
      username: userInfo.username,
      email: userInfo.email,
      role: userInfo.role,
      // Only store avatar if it's a URL, not a base64 string
      avatar: (userInfo.avatar && userInfo.avatar.startsWith('http')) ? userInfo.avatar : "",
      token: userInfo.token,
    };
    localStorage.setItem("user", JSON.stringify(minimalUser));
    setUser(minimalUser);
  };

  return (
    <Router>
      <UserContext.Provider value={{ user, setUserContext: updateUserData }}>
        <div className="App">
          <Header title="Closy" />
          <Routes>
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />
            <Route path="/logout" element={<Logout />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/" element={<ClothingItems />} />
            <Route path="/explore" element={<ExploreClothingItems />} />
            <Route path="/addClothingItem" element={<AddClothingItem />} />
            <Route path="/clothingItem/:id" element={<ClothingItemInfo />} />
            <Route path="/addOutfit" element={<AddOutfit />} />
            <Route path="/outfits" element={<OutfitList />} />
            <Route path="/outfit/:id" element={<OutfitInfo />} />
            <Route path="/stores" element={<ClothingStores />} />
            <Route path="/addClothingStore" element={<AddClothingStore />} />
            <Route path="/addClothingStoreLocation" element={<AddClothingStoreLocation />} />
            <Route path="/weather" element={<WeatherTab />} />
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/map" element={<Map />}></Route>
          </Routes>
          <nav>
            <Link to="/">Home</Link> |{' '}
            <Link to="/explore">Explore</Link> |{' '}
            <Link to="/register">Register</Link> |{' '}
            <Link to="/login">Login</Link> |{' '}
            <Link to="/outfits">Outfits</Link> |{' '}
            {user && (
              <>
                <Link to="/logout">Logout</Link> |{' '}
                <Link to="/profile">Profile</Link> |{' '}
                <Link to="/addClothingItem">Add Item</Link> |{' '}
                <Link to="/addClothingStore">Add Store</Link> |{' '}
                <Link to="/addClothingStoreLocation">Add Store location</Link> |{' '}
                <Link to="/stores">Stores</Link> |{' '}
                <Link to="/addOutfit">Add Outfit</Link>
              </>
            )}
          </nav>
        </div>
      </UserContext.Provider>
    </Router>
  );
}

export default App;