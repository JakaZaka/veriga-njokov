import React, { useState, useEffect } from 'react';
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
import Map from './components/Map.js';
import socket from './socket';
import ReceivedItemModal from './components/ReceivedItemModal.js';
import HomePage from './components/HomePage'; 



function App() {
  const [user, setUser] = useState(localStorage.user ? JSON.parse(localStorage.user) : null);
  const [receivedItem, setReceivedItem] = useState(null);
  const [modalIsOpen, setModalIsOpen] = useState(false);

  const showReceivedItemModal = (item) => {
    setReceivedItem(item);
    setModalIsOpen(true);
  }

  const closeReceivedItemModal = () => {
    setReceivedItem(null);
    setModalIsOpen(false);
  }

  useEffect(() => {
      // Add event listener once when component mounts
      socket.on('clothingItemTransferred', (item) => {
        console.log('🎁 Received item via socket:', item);
        showReceivedItemModal(item);
      });

      // Cleanup on unmount to prevent multiple listeners
      return () => {
        socket.off('clothingItemTransferred');
      };
  }, []);

  useEffect(() => {
  if (user && user._id) {
    socket.emit('login', user._id);
    console.log(`🔌 Socket login emitted for user ${user._id}`);
  }
  }, [user]);

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
      location: userInfo.location, // Assuming location is a string or null
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
            <Route path="/" element={<HomePage />} />
            <Route path="/clothingItems" element={<ClothingItems />} />
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

          <ReceivedItemModal
            isOpen={modalIsOpen}
            onRequestClose={closeReceivedItemModal}
            item={receivedItem}
          />
        </div>
      </UserContext.Provider>
    </Router>
  );
}

export default App;