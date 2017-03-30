package io.iotera.emma.smarthome.model.admin;

import javax.persistence.*;

@Entity
@Table(name = ESAdmin.NAME)
public class ESAdmin {

    public static final String NAME = "admin_tbl";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;

    @Column(nullable = false)
    protected String token;

}
