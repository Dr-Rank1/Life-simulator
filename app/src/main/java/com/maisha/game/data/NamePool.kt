package com.maisha.game.data

import com.maisha.game.data.model.Gender
import kotlin.random.Random

data class CountryNamePool(
    val maleFirstNames: List<String>,
    val femaleFirstNames: List<String>,
    val surnames: List<String>
) {
    val givenNames: List<String> = (maleFirstNames + femaleFirstNames).distinct()

    fun randomSurname(random: Random = Random.Default): String = surnames.random(random)

    fun randomMaleFirstName(random: Random = Random.Default): String =
        maleFirstNames.random(random)

    fun randomFemaleFirstName(random: Random = Random.Default): String =
        femaleFirstNames.random(random)

    fun randomFirstName(gender: Gender, random: Random = Random.Default): String = when (gender) {
        Gender.MALE -> randomMaleFirstName(random)
        Gender.FEMALE -> randomFemaleFirstName(random)
    }

    /** Any given name, used when gender is not yet chosen (siblings, classmates). */
    fun randomGivenName(random: Random = Random.Default): String = givenNames.random(random)

    fun randomFullName(gender: Gender, random: Random = Random.Default): String =
        "${randomFirstName(gender, random)} ${randomSurname(random)}"

    fun randomSiblingName(random: Random = Random.Default): String =
        "${randomGivenName(random)} ${randomSurname(random)}"
}

/**
 * Given names and surnames for every roster country.
 *
 * Unknown codes fall back to the US pool (not Kenya). Lists are de-duplicated on load.
 * Compound surnames such as "Dela Cruz" and "Van der Merwe" are single entries so
 * family generation can keep them intact.
 */
object NamePool {

    private const val MIN_GIVEN = 48
    private const val MIN_SURNAMES = 40

    private val pools: Map<String, CountryNamePool> = mapOf(
        "KE" to pool(
            male = """
                Brian, Kevin, Dennis, Collins, Victor, Ian, Samuel, Daniel, Joseph, Peter,
                James, David, Michael, George, Patrick, Eric, Alex, Tony, Kelvin, Elvis,
                Moses, Isaac, John, Paul, Stephen, Nicholas, Francis, Charles, Robert, Anthony,
                Philip, Benjamin, Emmanuel, Dominic, Felix, Leonard, Martin, Oscar, Reuben, Simon,
                Thomas, Vincent, Walter, Wesley, William, Caleb, Ethan, Baraka, Juma, Abdi,
                Hassan, Joshua, Mark, Andrew, Richard, Henry, Edward, Christopher, Timothy, Geoffrey
            """,
            female = """
                Wanjiku, Akinyi, Njeri, Amina, Faith, Grace, Mercy, Joy, Mary, Anne,
                Lucy, Jane, Ruth, Esther, Sarah, Wambui, Atieno, Chebet, Nyambura, Achieng,
                Halima, Fatuma, Wangari, Muthoni, Nafula, Zawadi, Neema, Purity, Sharon, Irene,
                Beatrice, Catherine, Diana, Elizabeth, Florence, Gloria, Helen, Ivy, Janet, Joyce,
                Lilian, Monica, Naomi, Olivia, Pauline, Rebecca, Stella, Teresa, Violet, Winnie,
                Yvonne, Alice, Brenda, Cynthia, Daisy, Edith, Fiona, Hannah, Isabel, Kimberly
            """,
            surnames = """
                Ochieng, Odhiambo, Onyango, Otieno, Okoth, Ouma, Owino, Oduor, Omondi, Ogutu,
                Kamau, Mwangi, Njoroge, Kariuki, Kimani, Maina, Gitau, Ngugi, Karanja, Mburu,
                Mutua, Wanjala, Wekesa, Barasa, Musyoka, Wambua, Kilonzo, Makau, Nzomo, Muli,
                Kiprop, Cheruiyot, Kiptoo, Koech, Rotich, Langat, Korir, Kiplagat, Ruto, Chepkwony,
                Abdi, Hassan, Mohamed, Ali, Juma, Were, Wafula, Nyongesa, Simiyu, Masinde
            """
        ),
        "NG" to pool(
            male = """
                Chidi, Emeka, Tunde, Ibrahim, Olu, Bayo, Kunle, Segun, Femi, Yusuf,
                Chinedu, Obinna, Tobi, Sola, Ade, Kelechi, Uche, Nnamdi, Babatunde, Musa,
                Ifeanyi, Chukwudi, Kayode, Sani, Ahmed, Olamide, Timi, Jide, Dele, Chukwuemeka,
                Olumide, Adebayo, Rotimi, Seun, Gbolahan, Ikenna, Obiora, Tochukwu, Usman, Suleiman,
                Farouk, Bashir, Nasir, Wale, Gbenga, Lanre, Dapo, Yemi, Chibuzo, Somto,
                Tayo, Idris, Habib, Mustapha, Lawal, Bode, Chike, Tope, Efe, Goke
            """,
            female = """
                Adaeze, Ngozi, Aisha, Funke, Zainab, Amara, Chioma, Bisi, Halima, Efe,
                Ifunanya, Nneka, Yetunde, Folake, Blessing, Chiamaka, Fatima, Ifeoma, Kemi, Tolu,
                Onyinye, Rukayat, Sade, Temitope, Uchechi, Yewande, Adanna, Bukola, Chidinma, Damilola,
                Abiodun, Bolanle, Chinyere, Dunni, Ezinne, Folasade, Habiba, Idayat, Jumoke, Kehinde,
                Lola, Maryam, Nkechi, Omolara, Peace, Queen, Ronke, Simi, Titilayo, Uju,
                Victoria, Wunmi, Motunrayo, Hadiza, Amaka, Bisola, Eniola, Funmilayo, Shade, Taiwo
            """,
            surnames = """
                Okonkwo, Adeyemi, Bello, Eze, Okafor, Nwosu, Musa, Adebayo, Chukwu, Yakubu,
                Ogunleye, Ibrahim, Okoro, Balogun, Nwachukwu, Afolabi, Obi, Lawal, Danjuma, Okeke,
                Adesina, Umar, Onyeka, Suleiman, Adewale, Ogundipe, Eke, Nnamani, Mohammed, Abdullahi,
                Okoye, Onwuka, Akinola, Bakare, Dike, Ekwueme, Fashola, Gbadamosi, Ibe, Jibril,
                Kalu, Madu, Nwafor, Obasanjo, Popoola, Salami, Tijani, Ugwu, Williams, Yusuf
            """
        ),
        "ZA" to pool(
            male = """
                Thabo, Sipho, Liam, Ethan, Siyabonga, Kagiso, Mandla, Johan, Pieter, David,
                Bandile, Lwazi, Tshepo, Bongani, Andile, James, Michael, Daniel, Ryan, Kyle,
                Willem, Francois, Ruan, Jaco, Nkosana, Mpho, Karabo, Lesego, Thulani, Vusi,
                Siphiwe, Mduduzi, Sello, Katlego, Tumelo, Lethabo, Sandile, Xolani, Jabulani, Themba,
                Hendrik, Christiaan, Riaan, Schalk, Dirk, Craig, Bradley, Justin, Dylan, Connor,
                Sibusiso, Nkosinathi, Lucky, Gift, Blessing, Prince, Junior, Neo, Teboho, Lebohang
            """,
            female = """
                Nomsa, Thandi, Lerato, Zanele, Emma, Olivia, Amahle, Lindiwe, Sarah, Grace,
                Naledi, Palesa, Boitumelo, Refilwe, Ayanda, Chloe, Mia, Hannah, Jessica, Amy,
                Annelie, Marike, Elize, Carmen, Precious, Blessing, Faith, Hope, Ntombi, Sibongile,
                Nokuthula, Thandeka, Busisiwe, Zodwa, Khanyisile, Minenhle, Nosipho, Puleng, Dineo, Kelebogile,
                Anika, Bianca, Danielle, Megan, Nicole, Stephanie, Tania, Michelle, Lauren, Kayla,
                Ayesha, Fatima, Zahra, Priya, Keshni, Thalia, Iminathi, Liyana, Rethabile, Amogelang
            """,
            surnames = """
                Nkosi, Dlamini, Mokoena, Botha, Van der Merwe, Pretorius, Khumalo, Mahlangu, Ndlovu, Smith,
                Naidoo, Pillay, Govender, Jacobs, Williams, Molefe, Sithole, Zulu, Van Wyk, Du Plessis,
                Nel, Coetzee, Mabaso, Radebe, Mthembu, Cele, Ngcobo, Mkhize, Buthelezi, Mashaba,
                Fourie, Venter, Kruger, Steyn, Joubert, Naicker, Reddy, Chetty, Abrahams, Adams,
                Maseko, Tshabalala, Baloyi, Maluleke, Nkuna, Sibiya, Zwane, Hadebe, Mofokeng, Sechaba
            """
        ),
        "EG" to pool(
            male = """
                Omar, Youssef, Ahmed, Karim, Hassan, Mahmoud, Ali, Tarek, Amir, Nader,
                Mohamed, Khaled, Mostafa, Hossam, Amr, Sherif, Walid, Ramy, Adel, Samir,
                Ibrahim, Fady, Mina, George, Mark, Andrew, Bassem, Hany, Ziad, Yassin,
                Abdelrahman, Hussein, Ismail, Kareem, Magdy, Nabil, Osama, Rami, Sameh, Tamer,
                Wael, Yahya, Zaki, Bishoy, Kyrillos, Shenouda, Youhanna, Fares, Seif, Hamza,
                Adam, Shady, Marwan, Anas, Loay, Maged, Hisham, Emad, Gaber, Nour
            """,
            female = """
                Fatima, Nour, Yasmin, Mariam, Salma, Dina, Hana, Layla, Rania, Amira,
                Sara, Nada, Farah, Malak, Jana, Lina, Maya, Nourhan, Reem, Habiba,
                Mona, Heba, Aya, Doaa, Esraa, Ghada, Hanan, Iman, Laila, Nadia,
                Basma, Dalia, Eman, Faten, Gehan, Hadeer, Inas, Jihan, Karima, Lubna,
                Magda, Naglaa, Ola, Pakinam, Rasha, Samah, Tahani, Wafaa, Yara, Zeinab,
                Marina, Demiana, Irini, Nermine, Veronica, Sandra, Nardin, Youstina, Basmala, Kenzy
            """,
            surnames = """
                Hassan, Ibrahim, Farouk, Mahmoud, Ali, Said, Khalil, Nasser, Fouad, Rashad,
                Mostafa, Ahmed, Youssef, Saleh, Mansour, Soliman, Gamal, Hegazy, Kamal, Zaki,
                Abdelrahman, Elmasry, Fathy, Osman, Tawfik, Abdelaziz, Elsayed, Fahmy, Gaber, Helmy,
                Ismail, Kamel, Lotfy, Megahed, Naguib, Omar, Qasem, Ramadan, Shawky, Tantawy,
                Wahba, Younis, Zaher, Boutros, Girgis, Hanna, Mikhail, Shenouda, Tadros, Wasily
            """
        ),
        "US" to pool(
            male = """
                James, Michael, David, Christopher, Daniel, Matthew, Andrew, Ryan, Tyler, Brandon,
                Joshua, Justin, Kevin, Brian, Jason, Eric, Jacob, Nathan, Aaron, Adam,
                Ethan, Noah, Liam, Mason, Logan, Aiden, Caleb, Dylan, Hunter, Connor,
                Jayden, Xavier, Andre, Jamal, Marcus, DeShawn, Elijah, Isaiah, Malik, Darius,
                Jose, Carlos, Luis, Diego, Miguel, Alejandro, Juan, Angel, Santiago, Mateo,
                Wei, Kenji, Hiro, Raj, Arjun, Omar, Ahmed, Hassan, Kwame, Jelani
            """,
            female = """
                Emily, Sarah, Jessica, Ashley, Megan, Lauren, Hannah, Olivia, Sophia, Emma,
                Madison, Abigail, Isabella, Ava, Mia, Chloe, Grace, Elizabeth, Samantha, Natalie,
                Rachel, Kayla, Brittany, Alexis, Victoria, Lily, Zoe, Hailey, Kaylee, Avery,
                Aaliyah, Destiny, Jasmine, Imani, Keisha, Nia, Amara, Zara, Fatima, Aisha,
                Sofia, Camila, Valentina, Lucia, Gabriela, Elena, Mariana, Carmen, Rosa, Paloma,
                Priya, Mei, Hana, Yuna, Amina, Layla, Noor, Keiko, Ananya, Sana
            """,
            surnames = """
                Smith, Johnson, Williams, Brown, Jones, Miller, Davis, Wilson, Anderson, Taylor,
                Thomas, Moore, Jackson, Martin, Lee, Thompson, White, Harris, Clark, Lewis,
                Robinson, Walker, Young, Allen, King, Wright, Scott, Hill, Green, Adams,
                Garcia, Rodriguez, Martinez, Hernandez, Lopez, Gonzalez, Perez, Sanchez, Ramirez, Torres,
                Nguyen, Kim, Patel, Chen, Wang, Singh, Ali, Khan, Washington, Brooks
            """
        ),
        "CA" to pool(
            male = """
                Liam, Noah, Oliver, Lucas, Benjamin, Ethan, William, James, Logan, Mason,
                Jacob, Alexander, Michael, Daniel, Matthew, Samuel, David, Joseph, Carter, Owen,
                Jack, Leo, Henry, Sebastian, Jackson, Aiden, Gabriel, Thomas, Charles, Nathan,
                Étienne, Olivier, Antoine, Philippe, Guillaume, Mathieu, Félix, Nicolas, Simon, Louis,
                Harpreet, Raj, Arjun, Wei, Jian, Hiroki, Omar, Ahmed, Kwame, Malik,
                Connor, Ryan, Tyler, Dylan, Hunter, Cole, Wyatt, Grayson, Hudson, Nolan
            """,
            female = """
                Olivia, Emma, Charlotte, Amelia, Sophia, Ava, Mia, Chloe, Lily, Abigail,
                Emily, Harper, Ella, Grace, Scarlett, Zoey, Hannah, Aria, Nora, Riley,
                Victoria, Claire, Sophie, Isabelle, Camille, Léa, Florence, Alice, Jade, Rose,
                Amélie, Juliette, Élodie, Béatrice, Geneviève, Sandrine, Mélanie, Valérie, Nadine, Céline,
                Priya, Aisha, Fatima, Mei, Yuna, Ananya, Noor, Hana, Sana, Amara,
                Mackenzie, Brooklyn, Paisley, Quinn, Avery, Hailey, Peyton, Sadie, Stella, Willow
            """,
            surnames = """
                Smith, Brown, Tremblay, Martin, Roy, Wilson, MacDonald, Campbell, Anderson, Taylor,
                Gagnon, Lee, Williams, Johnson, Thompson, White, Côté, Bouchard, Morin, Lavoie,
                Patel, Singh, Chen, Nguyen, Kim, Gauthier, Bélanger, Pelletier, Leblanc, Fortin,
                MacLeod, Fraser, Stewart, McKenzie, Ouellet, Dubé, Bergeron, Girard, Lefebvre, Caron,
                Ali, Khan, Wong, Li, Sharma, Brar, Diallo, Mensah, Rossi, Costa
            """
        ),
        "GB" to pool(
            male = """
                Oliver, Harry, George, Jack, Charlie, Thomas, William, James, Henry, Alfie,
                Noah, Leo, Oscar, Arthur, Freddie, Archie, Theo, Finley, Jacob, Isaac,
                Edward, Alexander, Samuel, Joseph, Daniel, Max, Lucas, Ethan, Mohammed, Reuben,
                Callum, Rhys, Owen, Dylan, Luca, Harrison, Teddy, Albie, Reggie, Hugo,
                Ayaan, Ibrahim, Yusuf, Omar, Ali, Arjun, Raj, Kwame, Malik, Adeel,
                Fraser, Hamish, Ewan, Angus, Finlay, Connor, Liam, Sean, Patrick, Cian
            """,
            female = """
                Olivia, Amelia, Isla, Ava, Emily, Sophia, Grace, Lily, Freya, Poppy,
                Mia, Ella, Sophie, Ivy, Rosie, Evie, Florence, Isabella, Charlotte, Daisy,
                Phoebe, Sienna, Elsie, Matilda, Alice, Ruby, Willow, Emilia, Millie, Harper,
                Aisha, Fatima, Maryam, Zara, Amina, Priya, Ananya, Noor, Hana, Layla,
                Siobhan, Niamh, Aoife, Orla, Caitlin, Megan, Ffion, Seren, Lowri, Eira,
                Imogen, Thea, Elodie, Penelope, Beatrice, Violet, Esme, Lyra, Mabel, Pearl
            """,
            surnames = """
                Smith, Jones, Williams, Taylor, Brown, Davies, Evans, Wilson, Thomas, Roberts,
                Johnson, Lewis, Walker, Robinson, Wood, Thompson, White, Watson, Jackson, Wright,
                Green, Harris, Cooper, King, Lee, Hughes, Edwards, Clarke, Morgan, Patel,
                Khan, Ali, Ahmed, Hussain, Singh, Begum, Rahman, Shah, Campbell, Stewart,
                MacLeod, Fraser, Murphy, O'Brien, Walsh, Kelly, Byrne, Gallagher, Akhtar, Chowdhury
            """
        ),
        "FR" to pool(
            male = """
                Louis, Gabriel, Raphaël, Arthur, Jules, Adam, Lucas, Hugo, Nathan, Ethan,
                Léo, Paul, Noah, Gabin, Sacha, Maël, Tom, Mohamed, Aaron, Timéo,
                Enzo, Théo, Maxime, Antoine, Nicolas, Alexandre, Mathis, Clément, Baptiste, Romain,
                Yanis, Mehdi, Karim, Amine, Bilal, Rayan, Ilyes, Nassim, Sofiane, Youssef,
                Étienne, Pierre, Jean, François, Olivier, Philippe, Guillaume, Sébastien, Christophe, Laurent,
                Marius, Noé, Augustin, Gaspard, Valentin, Adrien, Quentin, Florian, Rémi, Loïc
            """,
            female = """
                Emma, Jade, Louise, Alice, Chloé, Léa, Manon, Camille, Inès, Sarah,
                Lina, Rose, Anna, Mila, Julia, Léna, Zoé, Lola, Agathe, Juliette,
                Clara, Eva, Charlotte, Margaux, Ambre, Nina, Jeanne, Lucie, Elise, Pauline,
                Maëva, Yasmina, Amina, Fatima, Leila, Sofia, Nora, Salma, Myriam, Khadija,
                Céleste, Apolline, Capucine, Victoire, Romy, Léonie, Romane, Maëlys, Océane, Anaïs,
                Héloïse, Constance, Blanche, Solène, Justine, Marine, Aurélie, Céline, Sandrine, Valérie
            """,
            surnames = """
                Martin, Bernard, Dubois, Thomas, Robert, Richard, Petit, Durand, Leroy, Moreau,
                Simon, Laurent, Lefebvre, Michel, Garcia, David, Bertrand, Roux, Vincent, Fournier,
                Morel, Girard, André, Lefèvre, Mercier, Blanc, Guérin, Boyer, Garnier, Chevalier,
                François, Leclerc, Lambert, Bonnet, Dupuis, Rousseau, Clement, Fernandez, Lopez, Nguyen,
                Benali, Traoré, Diallo, Kouassi, Ndiaye, Dupont, Fontaine, Picard, Colin, Masson
            """
        ),
        "DE" to pool(
            male = """
                Lukas, Leon, Finn, Paul, Jonas, Felix, Max, Tim, Niklas, Jan,
                Ben, Luis, Noah, Elias, Emil, Henry, Theo, Oskar, Matteo, Anton,
                Julian, Moritz, David, Alexander, Sebastian, Tobias, Philipp, Simon, Fabian, Marcel,
                Mehmet, Ali, Yusuf, Mustafa, Emre, Can, Burak, Kerem, Omar, Ahmed,
                Karl, Friedrich, Heinrich, Otto, Wilhelm, Klaus, Wolfgang, Jürgen, Stefan, Andreas,
                Mats, Jannis, Lio, Milan, Levi, Samuel, Jonathan, Constantin, Valentin, Justus
            """,
            female = """
                Mia, Emma, Hannah, Sophia, Lena, Marie, Lea, Anna, Laura, Julia,
                Emilia, Lina, Mila, Clara, Luisa, Ella, Ida, Greta, Nora, Sophie,
                Johanna, Amelie, Charlotte, Marlene, Helena, Paula, Nele, Frieda, Pia, Jana,
                Aylin, Elif, Fatma, Zeynep, Yasemin, Meryem, Dilara, Leyla, Amina, Noor,
                Katharina, Elisabeth, Friederike, Helene, Margarete, Ingrid, Ursula, Petra, Sabine, Claudia,
                Mathilda, Lotte, Frida, Alma, Thea, Mira, Ronja, Juna, Hailey, Maja
            """,
            surnames = """
                Müller, Schmidt, Schneider, Fischer, Weber, Meyer, Wagner, Becker, Hoffmann, Schäfer,
                Koch, Bauer, Richter, Klein, Wolf, Schröder, Neumann, Schwarz, Zimmermann, Braun,
                Krüger, Hofmann, Hartmann, Lange, Schmitt, Werner, Schmitz, Krause, Meier, Lehmann,
                Huber, Kaiser, Fuchs, Peters, Lang, Scholz, Möller, Weiss, Jung, Hahn,
                Yilmaz, Kaya, Demir, Şahin, Çelik, Özdemir, Aydin, Arslan, Doğan, Kurt
            """
        ),
        "IN" to pool(
            male = """
                Arjun, Rahul, Vikram, Amit, Rohan, Sanjay, Karan, Dev, Anil, Raj,
                Aarav, Vivaan, Aditya, Krishna, Siddharth, Nikhil, Varun, Harsh, Manish, Pranav,
                Ravi, Suresh, Deepak, Ankit, Gaurav, Mohit, Yash, Kunal, Abhishek, Ishaan,
                Ayaan, Kabir, Vihaan, Reyansh, Shaurya, Dhruv, Atharv, Krish, Aryan, Advait,
                Mohammed, Ahmed, Imran, Farhan, Sameer, Rizwan, Omar, Yusuf, Hamza, Zain,
                Joseph, Thomas, Mathew, George, Abraham, Samuel, John, David, Daniel, Joshua
            """,
            female = """
                Priya, Ananya, Kavya, Meera, Sita, Lakshmi, Divya, Neha, Pooja, Riya,
                Aanya, Diya, Isha, Saanvi, Anika, Myra, Kiara, Aadhya, Pari, Navya,
                Shreya, Tanvi, Nisha, Sneha, Kriti, Aditi, Swati, Pallavi, Ritu, Sunita,
                Aisha, Fatima, Zara, Maryam, Sana, Noor, Hina, Alia, Yasmin, Nadia,
                Anjali, Bhavna, Chitra, Deepika, Esha, Gauri, Harini, Indira, Jaya, Kalyani,
                Mary, Elizabeth, Anna, Sarah, Rebecca, Grace, Hannah, Ruth, Esther, Lydia
            """,
            surnames = """
                Sharma, Patel, Singh, Kumar, Reddy, Gupta, Iyer, Nair, Das, Verma,
                Mehta, Joshi, Chopra, Malhotra, Banerjee, Chatterjee, Rao, Pillai, Desai, Shah,
                Agarwal, Kapoor, Bhat, Menon, Khan, Ali, Ahmed, Hussain, Sheikh, Ansari,
                Thomas, Mathew, George, Abraham, Joseph, Fernandes, D'Souza, Pereira, Naidu, Hegde,
                Mukherjee, Bose, Ghosh, Nambiar, Kulkarni, Patil, Jadhav, Chavan, Pawar, More
            """
        ),
        "JP" to pool(
            male = """
                Haruto, Yuto, Sota, Ren, Kaito, Hayato, Riku, Shota, Takumi, Daiki,
                Hiroto, Sora, Yuma, Kota, Minato, Asahi, Itsuki, Yamato, Haruki, Sosuke,
                Kenji, Takeshi, Hiroshi, Kazuki, Ryota, Naoki, Sho, Akira, Taro, Yuki,
                Haru, Hinata, Aoi, Subaru, Kenta, Daichi, Shun, Toru, Makoto, Issei,
                Kiyoshi, Masao, Shinji, Takashi, Yuji, Daisuke, Koji, Noboru, Osamu, Ryo,
                Eita, Haruma, Kosei, Raito, Rintaro, Toma, Wataru, Yu, Soma, Ibuki
            """,
            female = """
                Yui, Hina, Sakura, Aoi, Mei, Rin, Mio, Yuna, Sara, Nana,
                Himari, Koharu, Ichika, Akari, Yua, Riko, Saki, Miyu, Hana, Ema,
                Ayaka, Misaki, Nanami, Yuka, Rina, Kana, Mao, Airi, Nozomi, Haruka,
                Akane, Asuka, Chihiro, Emi, Fumiko, Hikari, Izumi, Kaori, Mai, Natsumi,
                Reina, Sayaka, Tomoko, Yoko, Yumiko, Aiko, Keiko, Michiko, Noriko, Sachiko,
                Honoka, Kokona, Kohana, Suzu, Umi, Wakana, Yuzuki, Sumire, Tsumugi, Kotone
            """,
            surnames = """
                Sato, Suzuki, Takahashi, Tanaka, Watanabe, Ito, Yamamoto, Nakamura, Kobayashi, Kato,
                Yoshida, Yamada, Sasaki, Yamaguchi, Matsumoto, Inoue, Kimura, Hayashi, Shimizu, Yamazaki,
                Mori, Abe, Ikeda, Hashimoto, Yamashita, Ishikawa, Nakajima, Ogawa, Goto, Okada,
                Hasegawa, Murakami, Kondo, Ishii, Saito, Maeda, Fujita, Endo, Aoki, Sakamoto,
                Fujii, Nishimura, Fukuda, Ota, Miura, Taniguchi, Okamoto, Matsuda, Nakagawa, Harada
            """
        ),
        "PH" to pool(
            male = """
                Jose, Mark, John, Angelo, Carlo, Miguel, Rafael, Paolo, Kenneth, Jerome,
                Christian, Daniel, Joshua, Nathan, Gabriel, Matthew, Ryan, Justin, Kyle, Lance,
                Andres, Emmanuel, Francis, Ivan, Jasper, Luis, Marco, Noel, Oscar, Vincent,
                Antonio, Benjie, Cesar, Dante, Eduardo, Ferdinand, Gerardo, Hector, Isidro, Julio,
                Lorenzo, Manuel, Nestor, Orlando, Pedro, Ramon, Sergio, Teodoro, Umberto, Victor,
                Abdul, Ahmad, Hassan, Ibrahim, Yusuf, Hiroshi, Wei, Kenji, Raj, Arjun
            """,
            female = """
                Maria, Angel, Grace, Joy, Kim, Hannah, Patricia, Nicole, Andrea, Catherine,
                Angela, Bianca, Christine, Diana, Elena, Faith, Gabrielle, Isabelle, Jasmine, Kristine,
                Lorraine, Michelle, Natalie, Olivia, Pauline, Queenie, Rose, Samantha, Therese, Vanessa,
                Ana, Beatriz, Carmen, Dolores, Esperanza, Fe, Gloria, Imelda, Josefina, Luz,
                Mercedes, Nieves, Pilar, Rosario, Soledad, Teresa, Corazon, Ligaya, Mahal, Mutya,
                Aisha, Fatima, Maryam, Siti, Mei, Hana, Yuna, Priya, Ananya, Noor
            """,
            surnames = """
                Santos, Reyes, Cruz, Bautista, Garcia, Mendoza, Torres, Flores, Ramos, Aquino,
                Dela Cruz, Gonzales, Lopez, Perez, Rivera, Castillo, Fernandez, Morales, Navarro, Villanueva,
                Domingo, Santiago, Lim, Tan, Sy, Chua, Ong, Go, Co, Yap,
                Magbanua, Magno, Manalo, Mercado, Pascual, Salazar, Tolentino, Valencia, Villar, Abad,
                Alcantara, Bernardo, Corpuz, Enriquez, Francisco, Gutierrez, Hernandez, Ignacio, Jimenez, Katigbak
            """
        ),
        "ID" to pool(
            male = """
                Budi, Agus, Rizky, Adi, Hendra, Dimas, Fajar, Rudi, Andi, Bayu,
                Ahmad, Dedi, Eko, Fauzan, Gilang, Hadi, Irfan, Joko, Kurniawan, Lukman,
                Muhammad, Nanda, Putra, Rama, Satria, Taufik, Wahyu, Yoga, Zaki, Arief,
                Bagus, Candra, Doni, Jaka, Farhan, Galih, Hanafi, Ilham, Jaya, Kusuma,
                Maulana, Nurdin, Prasetyo, Rahman, Surya, Teguh, Umar, Vino, Wira, Yudi,
                I Made, I Wayan, I Ketut, I Nyoman, Gede, Komang, Putu, Kadek, Made, Wayan
            """,
            female = """
                Siti, Dewi, Ayu, Rina, Maya, Lestari, Indah, Fitri, Wulan, Aisyah,
                Bunga, Citra, Dian, Eka, Farah, Gita, Hana, Intan, Kartika, Lina,
                Mega, Nisa, Oktavia, Rani, Sari, Tika, Utami, Yuni, Anisa, Bella,
                Cahaya, Dinda, Elsa, Fira, Gadis, Hesti, Ika, Jihan, Kirana, Laila,
                Mawar, Nia, Olivia, Prita, Qori, Ratna, Sinta, Tania, Vina, Winda,
                Ni Luh, Ni Made, Ni Wayan, Ni Ketut, Ni Nyoman, Putu, Kadek, Komang, Wati, Sri
            """,
            surnames = """
                Wijaya, Santoso, Pratama, Kusuma, Saputra, Hidayat, Nugroho, Setiawan, Rahman, Siregar,
                Gunawan, Halim, Iskandar, Kurniawan, Mahendra, Nasution, Prasetyo, Ramadhan, Suryadi, Utama,
                Wibowo, Yulianto, Zulkarnain, Hartono, Susanto, Wahyudi, Firmansyah, Abdullah, Hasan, Yusuf,
                Situmorang, Simanjuntak, Sitorus, Panjaitan, Ginting, Tarigan, Sembiring, Lubis, Harahap, Daulay,
                Putra, Putri, Ananda, Prakoso, Maulana, Fadillah, Syahputra, Firdaus, Alamsyah, Purnama
            """
        ),
        "BR" to pool(
            male = """
                Lucas, Gabriel, Miguel, Arthur, Pedro, Rafael, Felipe, Bruno, Diego, Thiago,
                Matheus, Gustavo, Leonardo, Enzo, Bernardo, Heitor, Davi, Samuel, João, Guilherme,
                Rodrigo, André, Carlos, Eduardo, Marcelo, Vinícius, Caio, Igor, Henrique, Daniel,
                Antônio, Francisco, José, Luiz, Paulo, Ricardo, Fernando, Alexandre, Roberto, Marcos,
                Mohammed, Ahmed, Omar, Youssef, Hiroshi, Wei, Kwame, Malik, Ivan, Nikita,
                Noah, Benício, Gael, Theo, Lorenzo, Murilo, Pietro, Cauã, Nicolas, Otávio
            """,
            female = """
                Maria, Ana, Julia, Beatriz, Larissa, Camila, Fernanda, Isabela, Luiza, Mariana,
                Alice, Helena, Valentina, Laura, Sophia, Manuela, Giovanna, Cecília, Eloá, Liz,
                Gabriela, Amanda, Bruna, Carolina, Letícia, Natália, Patrícia, Rafaela, Tatiana, Yasmin,
                Clara, Lara, Heloísa, Lorena, Melissa, Nicole, Olivia, Pietra, Rebeca, Sofia,
                Fatima, Aisha, Mei, Hana, Priya, Ananya, Amara, Noor, Keiko, Sana,
                Maitê, Aurora, Isis, Mirella, Maya, Antonella, Isabella, Lavínia, Agatha, Esther
            """,
            surnames = """
                Silva, Santos, Oliveira, Souza, Lima, Costa, Ferreira, Rodrigues, Almeida, Pereira,
                Carvalho, Gomes, Martins, Araújo, Melo, Barbosa, Ribeiro, Alves, Cardoso, Rocha,
                Dias, Nascimento, Moreira, Cavalcanti, Teixeira, Mendes, Nunes, Pinto, Azevedo, Correia,
                Fernandes, Vieira, Monteiro, Cunha, Freitas, Campos, Castro, Andrade, Nogueira, Batista,
                Guimarães, Magalhães, Peixoto, Tavares, Barros, Moura, Duarte, Farias, Lacerda, Siqueira
            """
        ),
        "MX" to pool(
            male = """
                Santiago, Mateo, Diego, Luis, Carlos, Javier, Miguel, Andrés, Ricardo, Fernando,
                Sebastián, Emiliano, Leonardo, Daniel, Alejandro, José, Juan, Pedro, Ángel, David,
                Eduardo, Francisco, Gabriel, Héctor, Iván, Jorge, Manuel, Óscar, Pablo, Raúl,
                Emilio, Gael, Iker, Maximiliano, Rodrigo, Salvador, Tomás, Vicente, Xavier, Yair,
                Armando, Benjamín, César, Damián, Esteban, Federico, Gerardo, Hugo, Ignacio, Joaquín,
                Omar, Ahmed, Wei, Hiroshi, Kwame, Raj, Arjun, Ivan, Nikita, Malik
            """,
            female = """
                Sofía, Valentina, Camila, Mariana, Lucía, Daniela, Paula, Andrea, Fernanda, Ximena,
                Regina, Renata, Victoria, Isabella, Emma, Mia, Natalia, Valeria, Aitana, Romina,
                Alejandra, Carolina, Elena, Gabriela, Jimena, Karla, Laura, María, Patricia, Rosa,
                Abril, Bárbara, Constanza, Delfina, Esperanza, Fátima, Guadalupe, Itzel, Julieta, Katia,
                Lupita, Monserrat, Nayeli, Paola, Quetzalli, Xóchitl, Selene, Tania, Úrsula, Violeta,
                Amina, Fatima, Noor, Priya, Mei, Hana, Amara, Sana, Layla, Yasmin
            """,
            surnames = """
                Hernández, García, Martínez, López, González, Rodríguez, Pérez, Sánchez, Ramírez, Torres,
                Flores, Rivera, Gómez, Díaz, Cruz, Morales, Reyes, Gutiérrez, Ortiz, Chávez,
                Ramos, Vargas, Castillo, Jiménez, Moreno, Romero, Herrera, Medina, Aguilar, Guerrero,
                Álvarez, Mendoza, Vega, Castro, Ruiz, Muñoz, Soto, Contreras, Salazar, Ortega,
                Peña, Ríos, Delgado, Navarro, Domínguez, Espinoza, Silva, Méndez, Cortés, Núñez
            """
        )
    )

    init {
        pools.forEach { (code, pool) ->
            require(pool.maleFirstNames.size >= MIN_GIVEN) {
                "$code male given names: ${pool.maleFirstNames.size}"
            }
            require(pool.femaleFirstNames.size >= MIN_GIVEN) {
                "$code female given names: ${pool.femaleFirstNames.size}"
            }
            require(pool.surnames.size >= MIN_SURNAMES) {
                "$code surnames: ${pool.surnames.size}"
            }
        }
    }

    fun getNamePool(countryCode: String): CountryNamePool =
        pools[countryCode] ?: pools.getValue("US")

    fun hasPool(countryCode: String): Boolean = countryCode in pools

    fun randomSurname(countryCode: String = "US"): String =
        getNamePool(countryCode).randomSurname()

    fun randomMaleFirstName(countryCode: String = "US"): String =
        getNamePool(countryCode).randomMaleFirstName()

    fun randomFemaleFirstName(countryCode: String = "US"): String =
        getNamePool(countryCode).randomFemaleFirstName()

    fun randomGivenName(countryCode: String = "US"): String =
        getNamePool(countryCode).randomGivenName()

    fun randomFullName(gender: Gender, countryCode: String = "US"): String =
        getNamePool(countryCode).randomFullName(gender)

    fun randomSiblingName(countryCode: String = "US"): String =
        getNamePool(countryCode).randomSiblingName()

    private fun pool(male: String, female: String, surnames: String) = CountryNamePool(
        maleFirstNames = names(male),
        femaleFirstNames = names(female),
        surnames = names(surnames)
    )

    private fun names(csv: String): List<String> =
        csv.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
}
